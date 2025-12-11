package pro.respawn.flowmvi.metrics

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.UnrecoverableException
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.metrics.Quantile.Q50
import pro.respawn.flowmvi.metrics.Quantile.Q90
import pro.respawn.flowmvi.metrics.Quantile.Q95
import pro.respawn.flowmvi.metrics.Quantile.Q99
import pro.respawn.flowmvi.metrics.api.ActionMetrics
import pro.respawn.flowmvi.metrics.api.ExceptionMetrics
import pro.respawn.flowmvi.metrics.api.IntentMetrics
import pro.respawn.flowmvi.metrics.api.LifecycleMetrics
import pro.respawn.flowmvi.metrics.api.Meta
import pro.respawn.flowmvi.metrics.api.Metrics
import pro.respawn.flowmvi.metrics.api.MetricsSchemaVersion
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.metrics.api.StateMetrics
import pro.respawn.flowmvi.metrics.api.SubscriptionMetrics
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue
import kotlin.uuid.Uuid

private sealed interface Event {
    data class Bootstrap(val duration: Duration) : Event
    data class IntentInterArrival(val duration: Duration) : Event
    data class IntentProcessed(val duration: Duration, val inQueue: Duration?) : Event
    data class ActionPlugin(val duration: Duration) : Event
    data class ActionDispatched(val duration: Duration, val inQueue: Duration?) : Event
    data class StateProcessed(val duration: Duration) : Event
    data class Subscription(val lifetimeSamples: List<Double>, val subscriberSample: Double) : Event
    data class RunDuration(val duration: Duration) : Event
    data class Recovery(val duration: Duration) : Event
}

internal class MetricsCollector<S : MVIState, I : MVIIntent, A : MVIAction>(
    val reportingScope: CoroutineScope,
    val offloadContext: CoroutineContext,
    private val bucketDuration: Duration,
    private val windowSeconds: Int,
    private val emaAlpha: Double,
    internal val clock: Clock, // for tests
    internal val timeSource: TimeSource,
) : Metrics, SynchronizedObject(), AutoCloseable {

    private val storeId: Uuid = Uuid.random()
    private val currentRun = atomic<Run?>(null)
    private val runId = atomic<String?>(null)

    // region counters
    private val intentPerf = PerformanceMetrics(windowSeconds, emaAlpha, bucketDuration, clock)
    private val intentDurations = P2QuantileEstimator(Q50.value, Q90.value, Q95.value, Q99.value)
    private val intentPluginOverhead = P2QuantileEstimator(Q50.value)
    private val intentPluginEma = Ema(emaAlpha)
    private val intentQueue = TimeMarkQueue()
    private val intentQueueEma = Ema(emaAlpha)
    private val intentInterArrivalEma = Ema(emaAlpha)
    private val intentInterArrival = P2QuantileEstimator(Q50.value)
    private val intentTotal = atomic(0L)
    private val intentProcessed = atomic(0L)
    private val intentDropped = atomic(0L)
    private val intentUndelivered = atomic(0L)
    private val intentOverflows = atomic(0L)
    private val intentInFlight = atomic(0)
    private val intentInFlightMax = atomic(0)
    private val intentBurst = atomic(BurstState(null, 0, 0))
    private val lastIntentEnqueue = atomic<TimeMark?>(null)
    private val intentBufferMaxOccupancy = atomic(0)

    private val actionPerf = PerformanceMetrics(windowSeconds, emaAlpha, bucketDuration, clock)
    private val actionDeliveries = P2QuantileEstimator(Q50.value, Q90.value, Q95.value, Q99.value)
    private val actionQueue = TimeMarkQueue()
    private val actionQueueEma = Ema(emaAlpha)
    private val actionQueueMedian = P2QuantileEstimator(Q50.value)
    private val actionPluginOverhead = P2QuantileEstimator(Q50.value)
    private val actionPluginEma = Ema(emaAlpha)
    private val actionSent = atomic(0L)
    private val actionDelivered = atomic(0L)
    private val actionUndelivered = atomic(0L)
    private val actionOverflows = atomic(0L)
    private val actionBufferMaxOccupancy = atomic(0)
    private val actionInFlight = atomic(0)

    private val statePerf = PerformanceMetrics(windowSeconds, emaAlpha, bucketDuration, clock)
    private val stateDurations = P2QuantileEstimator(Q50.value, Q90.value, Q95.value, Q99.value)
    private val stateTransitions = atomic(0L)
    private val stateVetoed = atomic(0L)

    private val subscribersMedian = P2QuantileEstimator(Q50.value)
    private val lifetimeMedian = P2QuantileEstimator(Q50.value)
    private val lifetimeEma = Ema(emaAlpha)
    private val subscribersState = atomic(
        SubscribersState(
            events = 0,
            current = 0,
            peak = 0,
            lastChange = timeSource.markNow(),
            weightedMillis = 0.0,
            totalMillis = 0.0,
        ),
    )

    private val startCount = atomic(0L)
    private val stopCount = atomic(0L)
    private val uptimeTotalMillis = atomic(0L)
    private val currentStart = atomic<TimeMark?>(null)
    private val firstStartAt = atomic<Instant?>(null)
    private val lifetimeRuns = P2QuantileEstimator(Q50.value)
    private val lifetimeRunEma = Ema(emaAlpha)
    private val bootstrapMedian = P2QuantileEstimator(Q50.value)
    private val bootstrapEma = Ema(emaAlpha)

    private val exceptionTotal = atomic(0L)
    private val exceptionHandled = atomic(0L)
    private val recoveryMedian = P2QuantileEstimator(Q50.value)
    private val recoveryEma = Ema(emaAlpha)
    // endregion

    // TODO: introduce "lazyDecorator" or "storeDecorator" to access this w/o mutability
    private val lastConfig = atomic<StoreConfiguration<MVIState>?>(null)

    /** Returns a decorator that wires metrics capture into store callbacks. */
    @OptIn(ExperimentalFlowMVIAPI::class)
    fun asDecorator(name: String?): PluginDecorator<S, I, A> = decorator {
        this.name = name
        onStart { child ->
            val channel = Channel<Event>(Channel.UNLIMITED)
            val job = reportingScope.launch(offloadContext) { for (event in channel) onEvent(event) }
            val id = Uuid.random().toString()
            runId.value = id
            val previous = currentRun.getAndSet(Run(id, channel, job))
            check(previous == null || previous.job.isCompleted) {
                "MetricsCollector started with previous run still active for store=${config.name}"
            }
            recordStart(child)
        }
        onStop { child, e -> recordStop(child, e) }
        onIntentEnqueue { child, intent -> recordIntentEnqueue(child, intent) }
        onIntent { child, intent -> recordIntent(child, intent) }
        onUndeliveredIntent { child, intent -> recordUndeliveredIntent(child, intent) }
        onAction { child, action -> recordAction(child, action) }
        onActionDispatch { child, action -> recordActionDispatch(child, action) }
        onUndeliveredAction { child, action -> recordUndeliveredAction(child, action) }
        onState { child, old, new -> recordState(child, old, new) }
        onSubscribe { child, count -> recordSubscribe(child, count) }
        onUnsubscribe { child, count -> recordUnsubscribe(child, count) }
        onException { child, e -> recordException(child, e) }
    }

    private suspend fun PipelineContext<S, I, A>.recordStart(child: StorePlugin<S, I, A>) {
        lastConfig.value = config
        firstStartAt.compareAndSet(null, clock.now())
        startCount.incrementAndGet()
        val startedAt = timeSource.markNow()
        currentStart.value = startedAt
        child.run { onStart() }
        send(Event.Bootstrap(startedAt.elapsedNow()))
    }

    private fun ShutdownContext<S, I, A>.recordStop(child: StorePlugin<S, I, A>, e: Exception?) {
        stopCount.incrementAndGet()
        currentStart.getAndSet(null)?.let { startedAt ->
            val runDurationMillis = startedAt.elapsedNow()
            uptimeTotalMillis.addAndGet(runDurationMillis.inWholeMilliseconds)
            send(Event.RunDuration(runDurationMillis))
        }
        child.run { onStop(e) }
        val run = currentRun.getAndSet(null) ?: return
        reportingScope.launch(offloadContext) {
            this@recordStop.awaitUntilClosed()
            run.channel.close()
            run.job.join()
        }
    }

    private fun recordIntentEnqueue(child: StorePlugin<S, I, A>, intent: I): I? {
        intentTotal.incrementAndGet()
        val mapped = child.onIntentEnqueue(intent) ?: run {
            intentDropped.incrementAndGet()
            return null
        }
        val nowMark = timeSource.markNow()
        updateIntentBurst(clock.now().epochSeconds)
        lastIntentEnqueue.getAndSet(nowMark)?.elapsedNow()?.let {
            send(Event.IntentInterArrival(it))
        }
        intentQueue.addLast(nowMark)
        updateIntentOccupancy()
        return mapped
    }

    private suspend fun PipelineContext<S, I, A>.recordIntent(child: StorePlugin<S, I, A>, intent: I): I? {
        val start = timeSource.markNow()
        val queueInstant = intentQueue.removeFirstOrNull()
        val queuedDuration = queueInstant?.elapsedNow()
        val inFlight = intentInFlight.incrementAndGet()
        intentInFlightMax.update { current -> maxOf(current, inFlight) }
        updateIntentOccupancy()
        val result = runCatching { child.run { onIntent(intent) } }.getOrElse {
            intentInFlight.decrementAndGet()
            updateIntentOccupancy()
            throw it
        }
        val durationMillis = start.elapsedNow()
        intentInFlight.decrementAndGet()
        intentProcessed.incrementAndGet()
        if (result == null) intentDropped.incrementAndGet()
        updateIntentOccupancy()
        send(Event.IntentProcessed(durationMillis, queuedDuration))
        return result
    }

    private fun ShutdownContext<S, I, A>.recordUndeliveredIntent(child: StorePlugin<S, I, A>, intent: I) {
        intentUndelivered.incrementAndGet()
        intentOverflows.incrementAndGet()
        intentQueue.removeFirstOrNull()
        updateIntentOccupancy()
        child.run { onUndeliveredIntent(intent) }
    }

    private suspend fun PipelineContext<S, I, A>.recordAction(child: StorePlugin<S, I, A>, action: A): A? {
        val start = timeSource.markNow()
        actionQueue.addLast(start)
        updateActionOccupancy()
        val result = child.run { onAction(action) }
        val duration = start.elapsedNow()
        if (result != null) actionSent.incrementAndGet()
        send(Event.ActionPlugin(duration))
        return result
    }

    private fun recordActionDispatch(child: StorePlugin<S, I, A>, action: A): A? {
        val dispatchStart = timeSource.markNow()
        val queued = actionQueue.removeFirstOrNull()
        val queueDurationMillis = queued?.elapsedNow()
        actionInFlight.incrementAndGet()
        updateActionOccupancy()
        val result = runCatching { child.onActionDispatch(action) }.getOrElse {
            actionInFlight.decrementAndGet()
            updateActionOccupancy()
            throw it
        }
        val dispatchDuration = dispatchStart.elapsedNow()
        val totalDurationMillis = queueDurationMillis?.let { it + dispatchDuration } ?: dispatchDuration
        send(Event.ActionDispatched(totalDurationMillis, queueDurationMillis))
        if (result != null) actionDelivered.incrementAndGet()
        actionInFlight.decrementAndGet()
        updateActionOccupancy()
        return result
    }

    private fun ShutdownContext<S, I, A>.recordUndeliveredAction(child: StorePlugin<S, I, A>, action: A) {
        actionUndelivered.incrementAndGet()
        actionOverflows.incrementAndGet()
        actionQueue.removeFirstOrNull()
        updateActionOccupancy()
        child.run { onUndeliveredAction(action) }
    }

    private suspend fun PipelineContext<S, I, A>.recordState(
        child: StorePlugin<S, I, A>,
        old: S,
        new: S,
    ): S? {
        val (result, duration) = timeSource.measureTimedValue {
            child.run { onState(old, new) }
        }
        stateTransitions.incrementAndGet()
        val vetoed = result == null || result === old
        if (vetoed) stateVetoed.incrementAndGet()
        send(Event.StateProcessed(duration))
        return result
    }

    private suspend fun PipelineContext<S, I, A>.recordSubscribe(child: StorePlugin<S, I, A>, count: Int) {
        handleSubscriptionChange(count)
        child.run { onSubscribe(count) }
    }

    private suspend fun PipelineContext<S, I, A>.recordUnsubscribe(child: StorePlugin<S, I, A>, count: Int) {
        handleSubscriptionChange(count)
        child.run { onUnsubscribe(count) }
    }

    private suspend fun PipelineContext<S, I, A>.recordException(
        child: StorePlugin<S, I, A>,
        e: Exception,
    ): Exception? {
        exceptionTotal.incrementAndGet()
        val (result, duration) = timeSource.measureTimedValue {
            child.run { onException(e) }
        }
        if (result == null) {
            exceptionHandled.incrementAndGet()
            send(Event.Recovery(duration))
        }
        return result
    }

    private fun send(event: Event) {
        currentRun.value?.channel.let {
            // todo: cleanup to self-contained class
            if (it == null && currentlyDebuggable) throw UnrecoverableException(
                message = "Metrics were attempted to be sent outside the store lifecycle, " +
                    "which should be impossible. If you detected this, please report to the maintainers. " +
                    "This will not crash with debuggable=false"
            )
            // on release, just drop, don't crash
            it?.trySend(event)
        }
    }

    private fun updateIntentOccupancy() {
        val occupancy = intentQueue.size + intentInFlight.value
        intentBufferMaxOccupancy.update { current -> maxOf(current, occupancy) }
    }

    private fun updateActionOccupancy() {
        val occupancy = actionQueue.size + actionInFlight.value
        actionBufferMaxOccupancy.update { current -> maxOf(current, occupancy) }
    }

    private fun updateIntentBurst(epochSeconds: Long) = intentBurst.update { previous ->
        if (previous.second == epochSeconds) {
            val current = previous.current + 1
            BurstState(epochSeconds, current, maxOf(previous.max, current))
        } else {
            BurstState(epochSeconds, 1, maxOf(previous.max, 1))
        }
    }

    private fun handleSubscriptionChange(newCount: Int) {
        val clampedCount = newCount.coerceAtLeast(0)
        var lifetimeSamples: List<Double> = emptyList()
        var subscriberSample = clampedCount.toDouble()
        subscribersState.update { state ->
            val now = timeSource.markNow()
            val elapsedMillis = state.lastChange.elapsedNow().inWholeMilliseconds.toDouble()
            val weighted = state.weightedMillis + state.current * elapsedMillis
            val total = state.totalMillis + elapsedMillis
            val removed = (state.current - clampedCount).coerceAtLeast(0)
            if (removed > 0) lifetimeSamples = List(removed) { elapsedMillis }
            subscriberSample = clampedCount.toDouble()
            SubscribersState(
                events = state.events + 1,
                current = clampedCount,
                peak = maxOf(state.peak, clampedCount),
                lastChange = now,
                weightedMillis = weighted,
                totalMillis = total,
            )
        }
        send(Event.Subscription(lifetimeSamples, subscriberSample))
    }

    private fun computeSubscribersAverage(state: SubscribersState): Double {
        if (state.totalMillis == 0.0) return state.current.toDouble()
        return state.weightedMillis / state.totalMillis
    }

    private suspend fun onEvent(event: Event) {
        when (event) {
            is Event.Bootstrap -> {
                bootstrapEma += event.duration
                bootstrapMedian.add(event.duration.msDouble)
            }
            is Event.IntentInterArrival -> {
                intentInterArrivalEma += event.duration
                intentInterArrival.add(event.duration.msDouble)
            }
            is Event.IntentProcessed -> {
                intentPerf.recordOperation(event.duration)
                intentDurations.add(event.duration.msDouble)
                intentPluginOverhead.add(event.duration.msDouble)
                intentPluginEma += event.duration
                event.inQueue?.let { intentQueueEma += it }
            }
            is Event.ActionPlugin -> {
                actionPluginEma += event.duration
                actionPluginOverhead.add(event.duration.msDouble)
            }
            is Event.ActionDispatched -> {
                actionPerf.recordOperation(event.duration)
                actionDeliveries.add(event.duration.msDouble)
                event.inQueue?.let {
                    actionQueueEma += it
                    actionQueueMedian.add(it.msDouble)
                }
            }
            is Event.StateProcessed -> {
                statePerf.recordOperation(event.duration)
                stateDurations.add(event.duration.msDouble)
            }
            is Event.Subscription -> {
                event.lifetimeSamples.forEach {
                    lifetimeEma += it
                    lifetimeMedian.add(it)
                }
                subscribersMedian.add(event.subscriberSample)
            }
            is Event.RunDuration -> {
                lifetimeRunEma += event.duration
                lifetimeRuns.add(event.duration.msDouble)
            }
            is Event.Recovery -> {
                recoveryEma += event.duration
                recoveryMedian.add(event.duration.msDouble)
            }
        }
    }

    override suspend fun snapshot() = withContext(offloadContext) {
        MetricsSnapshot(
            meta = Meta(
                schemaVersion = MetricsSchemaVersion.CURRENT,
                runId = runId.value,
                generatedAt = clock.now(),
                startTime = firstStartAt.value,
                storeName = lastConfig.value?.name,
                storeId = storeId.toString(),
                windowSeconds = windowSeconds,
                emaAlpha = emaAlpha.toFloat(),
            ),
            intents = IntentMetrics(
                total = intentTotal.value,
                processed = intentProcessed.value,
                dropped = intentDropped.value,
                undelivered = intentUndelivered.value,
                opsPerSecond = intentPerf.opsPerSecond(),
                durationAvg = intentPerf.averageTimeMillis.toDurationOrZero(),
                durationP50 = intentDurations.getQuantile(Q50.value).toDurationOrZero(),
                durationP90 = intentDurations.getQuantile(Q90.value).toDurationOrZero(),
                durationP95 = intentDurations.getQuantile(Q95.value).toDurationOrZero(),
                durationP99 = intentDurations.getQuantile(Q99.value).toDurationOrZero(),
                queueTimeAvg = intentQueueEma.value.toDurationOrZero(),
                inFlightMax = intentInFlightMax.value,
                interArrivalAvg = intentInterArrivalEma.value.toDurationOrZero(),
                interArrivalMedian = intentInterArrival.getQuantile(Q50.value).toDurationOrZero(),
                burstMax = intentBurst.value.max,
                bufferMaxOccupancy = intentBufferMaxOccupancy.value,
                bufferOverflows = intentOverflows.value,
                pluginOverheadAvg = intentPluginEma.value.toDurationOrZero(),
                pluginOverheadMedian = intentPluginOverhead.getQuantile(Q50.value).toDurationOrZero(),
            ),
            actions = ActionMetrics(
                sent = actionSent.value,
                delivered = actionDelivered.value,
                undelivered = actionUndelivered.value,
                opsPerSecond = actionPerf.opsPerSecond(),
                deliveryAvg = actionPerf.averageTimeMillis.toDurationOrZero(),
                deliveryP50 = actionDeliveries.getQuantile(Q50.value).toDurationOrZero(),
                deliveryP90 = actionDeliveries.getQuantile(Q90.value).toDurationOrZero(),
                deliveryP95 = actionDeliveries.getQuantile(Q95.value).toDurationOrZero(),
                deliveryP99 = actionDeliveries.getQuantile(Q99.value).toDurationOrZero(),
                queueTimeAvg = actionQueueEma.value.toDurationOrZero(),
                queueTimeMedian = actionQueueMedian.getQuantile(Q50.value).toDurationOrZero(),
                bufferMaxOccupancy = actionBufferMaxOccupancy.value,
                bufferOverflows = actionOverflows.value,
                pluginOverheadAvg = actionPluginEma.value.toDurationOrZero(),
                pluginOverheadMedian = actionPluginOverhead.getQuantile(Q50.value).toDurationOrZero(),
            ),
            state = StateMetrics(
                transitions = stateTransitions.value,
                transitionsVetoed = stateVetoed.value,
                updateAvg = statePerf.averageTimeMillis.toDurationOrZero(),
                updateP50 = stateDurations.getQuantile(Q50.value).toDurationOrZero(),
                updateP90 = stateDurations.getQuantile(Q90.value).toDurationOrZero(),
                updateP95 = stateDurations.getQuantile(Q95.value).toDurationOrZero(),
                updateP99 = stateDurations.getQuantile(Q99.value).toDurationOrZero(),
                opsPerSecond = statePerf.opsPerSecond(),
            ),
            subscriptions = subscribersState.value.let { subs ->
                SubscriptionMetrics(
                    events = subs.events,
                    current = subs.current,
                    peak = subs.peak,
                    lifetimeAvg = lifetimeEma.value.toDurationOrZero(),
                    lifetimeMedian = lifetimeMedian.getQuantile(Q50.value).toDurationOrZero(),
                    subscribersAvg = computeSubscribersAverage(subs),
                    subscribersMedian = subscribersMedian.getQuantile(Q50.value).takeUnless(Double::isNaN) ?: 0.0,
                )
            },
            lifecycle = LifecycleMetrics(
                startCount = startCount.value,
                stopCount = stopCount.value,
                uptimeTotal = uptimeTotalMillis.value.milliseconds,
                lifetimeCurrent = currentStart.value?.elapsedNow() ?: ZERO,
                lifetimeAvg = lifetimeRunEma.value.toDurationOrZero(),
                lifetimeMedian = lifetimeRuns.getQuantile(Q50.value).toDurationOrZero(),
                bootstrapAvg = bootstrapEma.value.toDurationOrZero(),
                bootstrapMedian = bootstrapMedian.getQuantile(Q50.value).toDurationOrZero(),
            ),
            exceptions = ExceptionMetrics(
                total = exceptionTotal.value,
                handled = exceptionHandled.value,
                recoveryLatencyAvg = recoveryEma.value.toDurationOrZero(),
                recoveryLatencyMedian = recoveryMedian.getQuantile(Q50.value).toDurationOrZero(),
            ),
            storeConfiguration = lastConfig.value,
        )
    }

    /** Clears all internal counters and estimators. */
    fun reset() = synchronized(this) {
        intentTotal.value = 0
        intentProcessed.value = 0
        intentDropped.value = 0
        intentUndelivered.value = 0
        intentOverflows.value = 0
        intentInFlight.value = 0
        intentInFlightMax.value = 0
        intentBurst.value = BurstState(null, 0, 0)
        lastIntentEnqueue.value = null
        intentBufferMaxOccupancy.value = 0
        actionSent.value = 0
        actionDelivered.value = 0
        actionUndelivered.value = 0
        actionOverflows.value = 0
        actionBufferMaxOccupancy.value = 0
        actionInFlight.value = 0
        stateTransitions.value = 0
        stateVetoed.value = 0
        subscribersState.value = SubscribersState(
            events = 0,
            current = 0,
            peak = 0,
            lastChange = timeSource.markNow(),
            weightedMillis = 0.0,
            totalMillis = 0.0,
        )
        startCount.value = 0
        stopCount.value = 0
        uptimeTotalMillis.value = 0
        currentStart.value = null
        exceptionTotal.value = 0
        exceptionHandled.value = 0
        intentQueue.clear()
        actionQueue.clear()
        intentPerf.reset()
        actionPerf.reset()
        statePerf.reset()
        intentDurations.clear()
        intentPluginOverhead.clear()
        intentPluginEma.reset()
        intentQueueEma.reset()
        intentInterArrivalEma.reset()
        intentInterArrival.clear()
        actionDeliveries.clear()
        actionQueueEma.reset()
        actionQueueMedian.clear()
        actionPluginOverhead.clear()
        actionPluginEma.reset()
        stateDurations.clear()
        subscribersMedian.clear()
        lifetimeMedian.clear()
        lifetimeEma.reset()
        lifetimeRuns.clear()
        lifetimeRunEma.reset()
        bootstrapMedian.clear()
        bootstrapEma.reset()
        recoveryMedian.clear()
        recoveryEma.reset()
    }

    override fun close() = currentRun.update {
        it?.channel?.close()
        it?.job?.cancel()
        null
    }

    private val currentlyDebuggable: Boolean get() = lastConfig.value?.debuggable == true
}

private data class SubscribersState(
    val events: Long,
    val current: Int,
    val peak: Int,
    val lastChange: TimeMark,
    val weightedMillis: Double,
    val totalMillis: Double,
)

private data class BurstState(val second: Long?, val current: Int, val max: Int)

private data class Run(val id: String, val channel: Channel<Event>, val job: Job)

internal class TimeMarkQueue : SynchronizedObject() {

    private val deque = ArrayDeque<TimeMark>()

    val size: Int
        get() = synchronized(this) { deque.size }

    fun addLast(value: TimeMark) = synchronized(this) { deque.addLast(value) }

    fun removeFirstOrNull(): TimeMark? = synchronized(this) {
        if (deque.isEmpty()) null else deque.removeFirst()
    }

    fun clear() = synchronized(this) { deque.clear() }
}

internal class Ema(private val alpha: Double) : SynchronizedObject() {

    var value: Double = Double.NaN
    var count: Long = 0L

    fun add(sample: Double) = synchronized(this) {
        value = if (count == 0L) sample else alpha * sample + (1 - alpha) * value
        count++
        Unit
    }

    fun reset() = synchronized(this) {
        value = Double.NaN
        count = 0L
    }

    operator fun plusAssign(sample: Double) = add(sample)
    operator fun plusAssign(sample: Duration) = add(sample.msDouble)
}

private inline val Duration.msDouble get() = toDouble(DurationUnit.MILLISECONDS)
private fun Double.toDurationOrZero(): Duration = if (isNaN()) ZERO else milliseconds
