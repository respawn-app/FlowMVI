package pro.respawn.flowmvi.metrics

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.atomicfu.update
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.uuid.Uuid

private const val Q50 = 0.5
private const val Q90 = 0.9
private const val Q95 = 0.95
private const val Q99 = 0.99

private sealed interface Event {
    data class Bootstrap(val millis: Double) : Event
    data class IntentInterArrival(val millis: Double) : Event
    data class IntentProcessed(val durationMillis: Double, val queueMillis: Double?) : Event
    data class ActionPlugin(val durationMillis: Double) : Event
    data class ActionDispatched(val totalMillis: Double, val queueMillis: Double?) : Event
    data class StateProcessed(val durationMillis: Double) : Event
    data class Subscription(val lifetimeSamples: List<Double>, val subscriberSample: Double) : Event
    data class RunDuration(val millis: Double) : Event
    data class Recovery(val millis: Double) : Event
    data class Snapshot(val meta: Meta, val reply: CompletableDeferred<MetricsSnapshot>) : Event
    data object Reset : Event
    data object Closing : Event
}

internal class MetricsCollector<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val reportingScope: CoroutineScope,
    private val sink: MetricsSink,
    private val offloadContext: CoroutineContext = Dispatchers.Default,
    val storeName: String? = null,
    private val windowSeconds: Int = 60,
    private val emaAlpha: Double = 0.1,
    private val clock: Clock = Clock.System,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val lockEnabled: Boolean = true,
) {

    val storeId: Uuid = Uuid.random()

    private val monitorLock = SynchronizedObject()

    private val actor = Channel<Event>(Channel.UNLIMITED)

    private val actorStarted = atomic(false)

    @Volatile
    private var actorJob: Job? = null

    // region counters
    private val intentPerf = PerformanceMetrics(windowSeconds, emaAlpha)
    private val intentDurations = P2QuantileEstimator(Q50, Q90, Q95, Q99)
    private val intentPluginOverhead = P2QuantileEstimator(Q50)
    private val intentPluginEma = Ema(emaAlpha)
    private val intentQueue = TimeMarkQueue()
    private val intentQueueEma = Ema(emaAlpha)
    private val intentInterArrivalEma = Ema(emaAlpha)
    private val intentInterArrival = P2QuantileEstimator(Q50)
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

    private val actionPerf = PerformanceMetrics(windowSeconds, emaAlpha)
    private val actionDeliveries = P2QuantileEstimator(Q50, Q90, Q95, Q99)
    private val actionQueue = TimeMarkQueue()
    private val actionQueueEma = Ema(emaAlpha)
    private val actionQueueMedian = P2QuantileEstimator(Q50)
    private val actionPluginOverhead = P2QuantileEstimator(Q50)
    private val actionPluginEma = Ema(emaAlpha)
    private val actionSent = atomic(0L)
    private val actionDelivered = atomic(0L)
    private val actionUndelivered = atomic(0L)
    private val actionOverflows = atomic(0L)
    private val actionBufferMaxOccupancy = atomic(0)
    private val actionInFlight = atomic(0)

    private val statePerf = PerformanceMetrics(windowSeconds, emaAlpha)
    private val stateDurations = P2QuantileEstimator(Q50, Q90, Q95, Q99)
    private val stateTransitions = atomic(0L)
    private val stateVetoed = atomic(0L)

    private val subscribersMedian = P2QuantileEstimator(Q50)
    private val lifetimeMedian = P2QuantileEstimator(Q50)
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
    private val lifetimeRuns = P2QuantileEstimator(Q50)
    private val lifetimeRunEma = Ema(emaAlpha)
    private val bootstrapMedian = P2QuantileEstimator(Q50)
    private val bootstrapEma = Ema(emaAlpha)

    private val exceptionTotal = atomic(0L)
    private val exceptionHandled = atomic(0L)
    private val recoveryMedian = P2QuantileEstimator(Q50)
    private val recoveryEma = Ema(emaAlpha)
    // endregion

    private val storeConfiguration = atomic<StoreConfiguration<out MVIState>?>(null)

    /** Returns a decorator that wires metrics capture into store callbacks. */
    @OptIn(ExperimentalFlowMVIAPI::class)
    fun asDecorator(name: String? = "MetricsDecorator"): PluginDecorator<S, I, A> = decorator {
        this.name = name
        onStart { child -> recordStart(child) }
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
        startActorOnce()
        val startedAt = timeSource.markNow()
        currentStart.value = startedAt
        startCount.incrementAndGet()
        storeConfiguration.value = config
        child.run { onStart() }
        val durationMillis = startedAt.elapsedNow().inWholeMilliseconds.toDouble()
        sendCommand(Event.Bootstrap(durationMillis))
    }

    private fun ShutdownContext<S, I, A>.recordStop(child: StorePlugin<S, I, A>, e: Exception?) {
        stopCount.incrementAndGet()
        currentStart.getAndSet(null)?.let { startedAt ->
            val runDurationMillis = startedAt.elapsedNow().inWholeMilliseconds
            uptimeTotalMillis.addAndGet(runDurationMillis)
            sendCommand(Event.RunDuration(runDurationMillis.toDouble()))
        }
        sendCommand(Event.Closing)
        child.run { onStop(e) }
    }

    private fun recordIntentEnqueue(child: StorePlugin<S, I, A>, intent: I): I? {
        intentTotal.incrementAndGet()
        val mapped = child.onIntentEnqueue(intent) ?: run {
            intentDropped.incrementAndGet()
            return null
        }
        val nowMark = timeSource.markNow()
        updateIntentBurst(clock.now().epochSeconds)
        lastIntentEnqueue.getAndSet(nowMark)?.elapsedNow()?.inWholeMilliseconds?.toDouble()?.let {
            sendCommand(Event.IntentInterArrival(it))
        }
        intentQueue.addLast(nowMark)
        updateIntentOccupancy()
        return mapped
    }

    private suspend fun PipelineContext<S, I, A>.recordIntent(child: StorePlugin<S, I, A>, intent: I): I? {
        val start = timeSource.markNow()
        val queueInstant = intentQueue.removeFirstOrNull()
        val queueDurationMillis = queueInstant?.elapsedNow()?.inWholeMilliseconds?.toDouble()
        val inFlight = intentInFlight.incrementAndGet()
        intentInFlightMax.update { current -> maxOf(current, inFlight) }
        updateIntentOccupancy()
        val result = runCatching { child.run { onIntent(intent) } }.getOrElse {
            intentInFlight.decrementAndGet()
            updateIntentOccupancy()
            throw it
        }
        val durationMillis = start.elapsedNow().inWholeMilliseconds.toDouble()
        intentInFlight.decrementAndGet()
        intentProcessed.incrementAndGet()
        if (result == null) intentDropped.incrementAndGet()
        updateIntentOccupancy()
        sendCommand(Event.IntentProcessed(durationMillis, queueDurationMillis))
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
        val durationMillis = start.elapsedNow().inWholeMilliseconds.toDouble()
        if (result != null) actionSent.incrementAndGet()
        sendCommand(Event.ActionPlugin(durationMillis))
        return result
    }

    private fun recordActionDispatch(child: StorePlugin<S, I, A>, action: A): A? {
        val dispatchStart = timeSource.markNow()
        val queued = actionQueue.removeFirstOrNull()
        val queueDurationMillis = queued?.elapsedNow()?.inWholeMilliseconds?.toDouble()
        actionInFlight.incrementAndGet()
        updateActionOccupancy()
        val result = runCatching { child.onActionDispatch(action) }.getOrElse {
            actionInFlight.decrementAndGet()
            updateActionOccupancy()
            throw it
        }
        val dispatchDuration = dispatchStart.elapsedNow().inWholeMilliseconds.toDouble()
        val totalDurationMillis = queueDurationMillis?.let { it + dispatchDuration } ?: dispatchDuration
        sendCommand(Event.ActionDispatched(totalDurationMillis, queueDurationMillis))
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
        val start = clock.now()
        val result = runCatching { child.run { onState(old, new) } }.getOrElse { throw it }
        val end = clock.now()
        val durationMillis = (end - start).inWholeMilliseconds.toDouble()
        stateTransitions.incrementAndGet()
        val vetoed = result == null || result === old
        if (vetoed) stateVetoed.incrementAndGet()
        sendCommand(Event.StateProcessed(durationMillis))
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
        val start = clock.now()
        val result = child.run { onException(e) }
        if (result == null) {
            val durationMillis = (clock.now() - start).inWholeMilliseconds.toDouble()
            exceptionHandled.incrementAndGet()
            sendCommand(Event.Recovery(durationMillis))
        }
        return result
    }

    /** Produces a snapshot of all metrics and forwards it to [sink]. */
    suspend fun snapshot(meta: Meta = defaultMeta()): MetricsSnapshot {
        val reply = CompletableDeferred<MetricsSnapshot>()
        sendCommand(Event.Snapshot(meta, reply))
        return reply.await()
    }

    /** Clears all internal counters and estimators. */
    fun reset() {
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
        sendCommand(Event.Reset)
    }

    /** Builds a [Meta] instance using the collector defaults. */
    fun defaultMeta(
        generatedAt: Instant = clock.now(),
        name: String? = storeName,
        id: String? = storeId.toString(),
    ): Meta = Meta(
        generatedAt = generatedAt,
        storeName = name,
        storeId = id,
        windowSeconds = windowSeconds,
        emaAlpha = emaAlpha.toFloat(),
    )

    @OptIn(DelicateCoroutinesApi::class)
    private fun startActorOnce() = withMonitor {
        if (!actorStarted.compareAndSet(expect = false, update = true)) return@withMonitor
        actorJob = reportingScope.launch(offloadContext) { processCommands(actor) }
    }

    private fun sendCommand(command: Event) {
        val result = actor.trySend(command)
        if (result.isFailure) actor.trySend(command)
    }

    private suspend fun processCommands(channel: Channel<Event>) {
        for (command in channel) when (command) {
            is Event.Bootstrap -> {
                bootstrapEma += command.millis
                bootstrapMedian.add(command.millis)
            }

            is Event.IntentInterArrival -> {
                intentInterArrivalEma += command.millis
                intentInterArrival.add(command.millis)
            }

            is Event.IntentProcessed -> {
                intentPerf.recordOperation(command.durationMillis.toLong())
                intentDurations.add(command.durationMillis)
                intentPluginOverhead.add(command.durationMillis)
                intentPluginEma += command.durationMillis
                command.queueMillis?.let { intentQueueEma += it }
            }

            is Event.ActionPlugin -> {
                actionPluginEma += command.durationMillis
                actionPluginOverhead.add(command.durationMillis)
            }

            is Event.ActionDispatched -> {
                actionPerf.recordOperation(command.totalMillis.toLong())
                actionDeliveries.add(command.totalMillis)
                command.queueMillis?.let {
                    actionQueueEma += it
                    actionQueueMedian.add(it)
                }
            }

            is Event.StateProcessed -> {
                statePerf.recordOperation(command.durationMillis.toLong())
                stateDurations.add(command.durationMillis)
            }

            is Event.Subscription -> {
                command.lifetimeSamples.forEach {
                    lifetimeEma += it
                    lifetimeMedian.add(it)
                }
                subscribersMedian.add(command.subscriberSample)
            }

            is Event.RunDuration -> {
                lifetimeRunEma += command.millis
                lifetimeRuns.add(command.millis)
            }

            is Event.Recovery -> {
                recoveryEma += command.millis
                recoveryMedian.add(command.millis)
            }

            is Event.Snapshot -> {
                val snapshot = buildSnapshot(command.meta)
                sink.emit(snapshot)
                command.reply.complete(snapshot)
            }

            Event.Reset -> resetMetrics()
            Event.Closing -> continue
        }
    }

    private fun resetMetrics() {
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

    private suspend fun buildSnapshot(meta: Meta): MetricsSnapshot {
        val subs = subscribersState.value
        // stopship: snapshot can be in an inconsistent state during read, needs its own lock for invariant
        return MetricsSnapshot(
            meta = meta,
            intents = IntentMetrics(
                total = intentTotal.value,
                processed = intentProcessed.value,
                dropped = intentDropped.value,
                undelivered = intentUndelivered.value,
                opsPerSecond = intentPerf.opsPerSecond(),
                durationAvg = intentPerf.averageTimeMillis.toDurationOrZero(),
                durationP50 = intentDurations.getQuantile(Q50).toDurationOrZero(),
                durationP90 = intentDurations.getQuantile(Q90).toDurationOrZero(),
                durationP95 = intentDurations.getQuantile(Q95).toDurationOrZero(),
                durationP99 = intentDurations.getQuantile(Q99).toDurationOrZero(),
                queueTimeAvg = intentQueueEma.value.toDurationOrZero(),
                inFlightMax = intentInFlightMax.value,
                interArrivalAvg = intentInterArrivalEma.value.toDurationOrZero(),
                interArrivalMedian = intentInterArrival.getQuantile(Q50).toDurationOrZero(),
                burstMax = intentBurst.value.max,
                bufferMaxOccupancy = intentBufferMaxOccupancy.value,
                bufferOverflows = intentOverflows.value,
                pluginOverheadAvg = intentPluginEma.value.toDurationOrZero(),
                pluginOverheadMedian = intentPluginOverhead.getQuantile(Q50).toDurationOrZero(),
            ),
            actions = ActionMetrics(
                sent = actionSent.value,
                delivered = actionDelivered.value,
                undelivered = actionUndelivered.value,
                opsPerSecond = actionPerf.opsPerSecond(),
                deliveryAvg = actionPerf.averageTimeMillis.toDurationOrZero(),
                deliveryP50 = actionDeliveries.getQuantile(Q50).toDurationOrZero(),
                deliveryP90 = actionDeliveries.getQuantile(Q90).toDurationOrZero(),
                deliveryP95 = actionDeliveries.getQuantile(Q95).toDurationOrZero(),
                deliveryP99 = actionDeliveries.getQuantile(Q99).toDurationOrZero(),
                queueTimeAvg = actionQueueEma.value.toDurationOrZero(),
                queueTimeMedian = actionQueueMedian.getQuantile(Q50).toDurationOrZero(),
                bufferMaxOccupancy = actionBufferMaxOccupancy.value,
                bufferOverflows = actionOverflows.value,
                pluginOverheadAvg = actionPluginEma.value.toDurationOrZero(),
                pluginOverheadMedian = actionPluginOverhead.getQuantile(Q50).toDurationOrZero(),
            ),
            state = StateMetrics(
                transitions = stateTransitions.value,
                transitionsVetoed = stateVetoed.value,
                updateAvg = statePerf.averageTimeMillis.toDurationOrZero(),
                updateP50 = stateDurations.getQuantile(Q50).toDurationOrZero(),
                updateP90 = stateDurations.getQuantile(Q90).toDurationOrZero(),
                updateP95 = stateDurations.getQuantile(Q95).toDurationOrZero(),
                updateP99 = stateDurations.getQuantile(Q99).toDurationOrZero(),
                opsPerSecond = statePerf.opsPerSecond(),
            ),
            subscriptions = SubscriptionMetrics(
                events = subs.events,
                current = subs.current,
                peak = subs.peak,
                lifetimeAvg = lifetimeEma.value.toDurationOrZero(),
                lifetimeMedian = lifetimeMedian.getQuantile(Q50).toDurationOrZero(),
                subscribersAvg = computeSubscribersAverage(subs),
                subscribersMedian = subscribersMedian.getQuantile(Q50).takeUnless(Double::isNaN) ?: 0.0,
            ),
            lifecycle = LifecycleMetrics(
                startCount = startCount.value,
                stopCount = stopCount.value,
                uptimeTotal = uptimeTotalMillis.value.milliseconds,
                lifetimeCurrent = currentStart.value?.elapsedNow() ?: ZERO,
                lifetimeAvg = lifetimeRunEma.value.toDurationOrZero(),
                lifetimeMedian = lifetimeRuns.getQuantile(Q50).toDurationOrZero(),
                bootstrapAvg = bootstrapEma.value.toDurationOrZero(),
                bootstrapMedian = bootstrapMedian.getQuantile(Q50).toDurationOrZero(),
            ),
            exceptions = ExceptionMetrics(
                total = exceptionTotal.value,
                handled = exceptionHandled.value,
                recoveryLatencyAvg = recoveryEma.value.toDurationOrZero(),
                recoveryLatencyMedian = recoveryMedian.getQuantile(Q50).toDurationOrZero(),
            ),
            storeConfiguration = storeConfiguration.value,
        )
    }

    private fun updateIntentOccupancy() {
        val occupancy = intentQueue.size + intentInFlight.value
        intentBufferMaxOccupancy.update { current -> maxOf(current, occupancy) }
    }

    private fun updateActionOccupancy() {
        val occupancy = actionQueue.size + actionInFlight.value
        actionBufferMaxOccupancy.update { current -> maxOf(current, occupancy) }
    }

    private fun updateIntentBurst(epochSeconds: Long) {
        intentBurst.update { previous ->
            if (previous.second == epochSeconds) {
                val current = previous.current + 1
                BurstState(epochSeconds, current, maxOf(previous.max, current))
            } else {
                BurstState(epochSeconds, 1, maxOf(previous.max, 1))
            }
        }
    }

    private fun handleSubscriptionChange(newCount: Int) {
        var lifetimeSamples: List<Double> = emptyList()
        var subscriberSample = newCount.toDouble()
        subscribersState.update { state ->
            val now = timeSource.markNow()
            val elapsedMillis = state.lastChange.elapsedNow().inWholeMilliseconds.toDouble()
            val weighted = state.weightedMillis + state.current * elapsedMillis
            val total = state.totalMillis + elapsedMillis
            val removed = (state.current - newCount).coerceAtLeast(0)
            if (removed > 0) {
                lifetimeSamples = List(removed) { elapsedMillis }
            }
            subscriberSample = newCount.toDouble()
            SubscribersState(
                events = state.events + 1,
                current = newCount,
                peak = maxOf(state.peak, newCount),
                lastChange = now,
                weightedMillis = weighted,
                totalMillis = total,
            )
        }
        sendCommand(Event.Subscription(lifetimeSamples, subscriberSample))
    }

    private fun computeSubscribersAverage(state: SubscribersState): Double {
        if (state.totalMillis == 0.0) return state.current.toDouble()
        return state.weightedMillis / state.totalMillis
    }

    private inline fun <T> withMonitor(crossinline block: () -> T): T =
        if (lockEnabled) synchronized(monitorLock) { block() } else block()

    private fun Double.toDuration(): Duration = this.milliseconds

    private fun Double.toDurationOrZero(): Duration = if (isNaN()) ZERO else toDuration()

    private fun Ema.reset() {
        value = Double.NaN
        count = 0L
    }
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

private class TimeMarkQueue : SynchronizedObject() {
    private val deque = ArrayDeque<TimeMark>()

    val size: Int
        get() = synchronized(this) { deque.size }

    fun addLast(value: TimeMark) = synchronized(this) { deque.addLast(value) }

    fun removeFirstOrNull(): TimeMark? = synchronized(this) {
        if (deque.isEmpty()) null else deque.removeFirst()
    }

    fun clear() = synchronized(this) { deque.clear() }
}

private class Ema(private val alpha: Double) {

    var value: Double = Double.NaN
    var count: Long = 0

    fun add(sample: Double) {
        value = if (count == 0L) sample else alpha * sample + (1 - alpha) * value
        count++
    }

    operator fun plusAssign(sample: Double) = add(sample)
    operator fun plusAssign(sample: Duration) = add(sample.inWholeMilliseconds.toDouble())
}
