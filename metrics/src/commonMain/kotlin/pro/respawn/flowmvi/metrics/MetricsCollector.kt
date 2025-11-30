package pro.respawn.flowmvi.metrics

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import kotlin.collections.ArrayDeque
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val Q50 = 0.5
private const val Q90 = 0.9
private const val Q95 = 0.95
private const val Q99 = 0.99

/**
 * Collects store-level runtime metrics and exposes a decorator to hook into store callbacks.
 *
 * @property storeName logical name used in generated [Meta]
 * @property windowSeconds sliding window for throughput metrics
 * @property emaAlpha smoothing factor for exponential moving averages
 * @property clock time source for measurements
 * @property sink sink that receives snapshots produced by [snapshot]
 */
public class MetricsCollector<S : MVIState, I : MVIIntent, A : MVIAction>(
    public val storeName: String? = null,
    private val windowSeconds: Int = 60,
    private val emaAlpha: Double = 0.1,
    private val clock: Clock = Clock.System,
    private val sink: MetricsSink = Sink { _ -> },
) : SynchronizedObject() {

    /** Stable identifier generated per collector instance. */
    public val storeId: Uuid = Uuid.random()

    private val intentPerf = PerformanceMetrics(windowSeconds, emaAlpha)
    private val intentDurations = P2QuantileEstimator(Q50, Q90, Q95, Q99)
    private val intentPluginOverhead = P2QuantileEstimator(Q50)
    private val intentQueueTimes = ArrayDeque<Instant>()
    private val intentQueueEma = Ema(emaAlpha)
    private val intentInterArrivalEma = Ema(emaAlpha)
    private val intentInterArrival = P2QuantileEstimator(Q50)
    private var intentTotal: Long = 0
    private var intentProcessed: Long = 0
    private var intentDropped: Long = 0
    private var intentUndelivered: Long = 0
    private var intentOverflows: Long = 0
    private var intentInFlight: Int = 0
    private var intentInFlightMax: Int = 0
    private var intentBurstSecond: Long? = null
    private var intentBurstCurrent: Int = 0
    private var intentBurstMax: Int = 0
    private var lastIntentEnqueue: Instant? = null
    private var intentBufferMaxOccupancy: Int = 0

    private val actionPerf = PerformanceMetrics(windowSeconds, emaAlpha)
    private val actionDeliveries = P2QuantileEstimator(Q50, Q90, Q95, Q99)
    private val actionQueueTimes = ArrayDeque<Instant>()
    private val actionQueueEma = Ema(emaAlpha)
    private val actionQueueMedian = P2QuantileEstimator(Q50)
    private val actionPluginOverhead = P2QuantileEstimator(Q50)
    private val actionPluginEma = Ema(emaAlpha)
    private var actionSent: Long = 0
    private var actionDelivered: Long = 0
    private var actionUndelivered: Long = 0
    private var actionOverflows: Long = 0
    private var actionBufferMaxOccupancy: Int = 0
    private var actionInFlight: Int = 0

    private val statePerf = PerformanceMetrics(windowSeconds, emaAlpha)
    private val stateDurations = P2QuantileEstimator(Q50, Q90, Q95, Q99)
    private var stateTransitions: Long = 0
    private var stateVetoed: Long = 0

    private var subscriptionEvents: Long = 0
    private var currentSubscribers: Int = 0
    private var peakSubscribers: Int = 0
    private var lastSubscribersChange: Instant = clock.now()
    private var subscribersTimeWeighted: Double = 0.0
    private var subscribersTimeTotalMillis: Double = 0.0
    private val subscribersMedian = P2QuantileEstimator(Q50)
    private val lifetimeMedian = P2QuantileEstimator(Q50)
    private val lifetimeEma = Ema(emaAlpha)

    private var startCount: Long = 0
    private var stopCount: Long = 0
    private var uptimeTotal: Duration = ZERO
    private var currentStart: Instant? = null
    private val lifetimeRuns = P2QuantileEstimator(Q50)
    private val lifetimeRunEma = Ema(emaAlpha)
    private val bootstrapMedian = P2QuantileEstimator(Q50)
    private val bootstrapEma = Ema(emaAlpha)

    private var exceptionTotal: Long = 0
    private var exceptionHandled: Long = 0
    private val recoveryMedian = P2QuantileEstimator(Q50)
    private val recoveryEma = Ema(emaAlpha)

    private var storeConfiguration: StoreConfiguration<out MVIState>? = null

    /** Returns a decorator that wires metrics capture into store callbacks. */
    @OptIn(ExperimentalFlowMVIAPI::class)
    public fun asDecorator(): PluginDecorator<S, I, A> = decorator {
        name = "MetricsCollector"
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
        val startedAt = clock.now()
        synchronized(this@MetricsCollector) {
            currentStart = startedAt
            startCount++
            storeConfiguration = config
        }
        child.run { onStart() }
        val finishedAt = clock.now()
        val duration = finishedAt - startedAt
        synchronized(this@MetricsCollector) {
            bootstrapEma.add(duration.inWholeMilliseconds.toDouble())
            bootstrapMedian.add(duration.inWholeMilliseconds.toDouble())
        }
    }

    private fun ShutdownContext<S, I, A>.recordStop(child: StorePlugin<S, I, A>, e: Exception?) {
        synchronized(this@MetricsCollector) {
            stopCount++
            val startInstant = currentStart
            if (startInstant != null) {
                val now = clock.now()
                val runDuration = now - startInstant
                uptimeTotal += runDuration
                lifetimeRunEma.add(runDuration.inWholeMilliseconds.toDouble())
                lifetimeRuns.add(runDuration.inWholeMilliseconds.toDouble())
            }
            currentStart = null
        }
        child.run { onStop(e) }
    }

    private fun recordIntentEnqueue(child: StorePlugin<S, I, A>, intent: I): I? {
        synchronized(this) { intentTotal++ }
        val mapped = child.onIntentEnqueue(intent) ?: run {
            synchronized(this) { intentDropped++ }
            return null
        }
        val now = clock.now()
        synchronized(this) {
            val epoch = now.epochSeconds
            if (intentBurstSecond == epoch) {
                intentBurstCurrent++
            } else {
                intentBurstSecond = epoch
                intentBurstCurrent = 1
            }
            if (intentBurstCurrent > intentBurstMax) intentBurstMax = intentBurstCurrent
            lastIntentEnqueue?.let { previous ->
                val delta = now - previous
                intentInterArrivalEma.add(delta.inWholeMilliseconds.toDouble())
                intentInterArrival.add(delta.inWholeMilliseconds.toDouble())
            }
            lastIntentEnqueue = now
            intentQueueTimes.addLast(now)
            updateIntentOccupancyLocked()
        }
        return mapped
    }

    private suspend fun PipelineContext<S, I, A>.recordIntent(child: StorePlugin<S, I, A>, intent: I): I? {
        val start = clock.now()
        val queueInstant = synchronized(this@MetricsCollector) {
            val queued = if (intentQueueTimes.isEmpty()) null else intentQueueTimes.removeFirst()
            intentInFlight++
            if (intentInFlight > intentInFlightMax) intentInFlightMax = intentInFlight
            intentProcessed++
            updateIntentOccupancyLocked()
            queued
        }
        val queueDuration = queueInstant?.let { start - it }
        queueDuration?.let {
            synchronized(this@MetricsCollector) { intentQueueEma.add(it.inWholeMilliseconds.toDouble()) }
        }
        val result = runCatching { child.run { onIntent(intent) } }.getOrElse {
            synchronized(this@MetricsCollector) { intentInFlight-- }
            throw it
        }
        val end = clock.now()
        val durationMillis = (end - start).inWholeMilliseconds.toDouble()
        synchronized(this@MetricsCollector) {
            intentInFlight--
            intentPerf.recordOperation(durationMillis.toLong())
            intentDurations.add(durationMillis)
            intentPluginOverhead.add(durationMillis)
            if (result == null) intentDropped++
            updateIntentOccupancyLocked()
        }
        return result
    }

    private fun ShutdownContext<S, I, A>.recordUndeliveredIntent(child: StorePlugin<S, I, A>, intent: I) {
        synchronized(this@MetricsCollector) {
            intentUndelivered++
            intentOverflows++
            if (intentQueueTimes.isNotEmpty()) intentQueueTimes.removeFirst()
            updateIntentOccupancyLocked()
        }
        child.run { onUndeliveredIntent(intent) }
    }

    private suspend fun PipelineContext<S, I, A>.recordAction(child: StorePlugin<S, I, A>, action: A): A? {
        val start = clock.now()
        val result = child.run { onAction(action) }
        val end = clock.now()
        val durationMillis = (end - start).inWholeMilliseconds.toDouble()
        synchronized(this@MetricsCollector) {
            if (result != null) {
                actionSent++
                actionQueueTimes.addLast(end)
                actionBufferMaxOccupancy = maxOf(actionBufferMaxOccupancy, actionQueueTimes.size + actionInFlight)
            }
            actionPluginEma.add(durationMillis)
            actionPluginOverhead.add(durationMillis)
        }
        return result
    }

    private fun recordActionDispatch(child: StorePlugin<S, I, A>, action: A): A? {
        val dispatchStart = clock.now()
        val enqueueInstant = synchronized(this) {
            val queued = if (actionQueueTimes.isEmpty()) null else actionQueueTimes.removeFirst()
            queued?.let {
                val qd = dispatchStart - it
                actionQueueEma.add(qd.inWholeMilliseconds.toDouble())
                actionQueueMedian.add(qd.inWholeMilliseconds.toDouble())
            }
            actionInFlight++
            queued
        }
        val result = runCatching { child.onActionDispatch(action) }.getOrElse {
            synchronized(this) { actionInFlight-- }
            throw it
        }
        val dispatchEnd = clock.now()
        val totalDurationMillis = enqueueInstant?.let { (dispatchEnd - it).inWholeMilliseconds.toDouble() }
            ?: (dispatchEnd - dispatchStart).inWholeMilliseconds.toDouble()
        synchronized(this) {
            actionPerf.recordOperation(totalDurationMillis.toLong())
            actionDeliveries.add(totalDurationMillis)
            if (result != null) actionDelivered++
            actionInFlight--
            actionBufferMaxOccupancy = maxOf(actionBufferMaxOccupancy, actionQueueTimes.size + actionInFlight)
        }
        return result
    }

    private fun ShutdownContext<S, I, A>.recordUndeliveredAction(child: StorePlugin<S, I, A>, action: A) {
        synchronized(this@MetricsCollector) {
            actionUndelivered++
            actionOverflows++
            if (actionQueueTimes.isNotEmpty()) actionQueueTimes.removeFirst()
            actionBufferMaxOccupancy = maxOf(actionBufferMaxOccupancy, actionQueueTimes.size + actionInFlight)
        }
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
        synchronized(this@MetricsCollector) {
            stateTransitions++
            statePerf.recordOperation(durationMillis.toLong())
            stateDurations.add(durationMillis)
            if (result == null || result === old) stateVetoed++
        }
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
        synchronized(this@MetricsCollector) { exceptionTotal++ }
        val start = clock.now()
        val result = child.run { onException(e) }
        if (result == null) {
            val durationMillis = (clock.now() - start).inWholeMilliseconds.toDouble()
            synchronized(this@MetricsCollector) {
                exceptionHandled++
                recoveryEma.add(durationMillis)
                recoveryMedian.add(durationMillis)
            }
        }
        return result
    }

    /** Produces a snapshot of all metrics and forwards it to [sink]. */
    public fun snapshot(meta: Meta = defaultMeta()): MetricsSnapshot = synchronized(this) {
        MetricsSnapshot(
            meta = meta,
            intents = IntentMetrics(
                total = intentTotal,
                processed = intentProcessed,
                dropped = intentDropped,
                undelivered = intentUndelivered,
                opsPerSecond = intentPerf.opsPerSecond(),
                durationAvg = intentPerf.averageTimeMillis.toDurationOrZero(),
                durationP50 = intentDurations.getQuantile(Q50).toDurationOrZero(),
                durationP90 = intentDurations.getQuantile(Q90).toDurationOrZero(),
                durationP95 = intentDurations.getQuantile(Q95).toDurationOrZero(),
                durationP99 = intentDurations.getQuantile(Q99).toDurationOrZero(),
                queueTimeAvg = intentQueueEma.value.toDurationOrZero(),
                inFlightMax = intentInFlightMax,
                interArrivalAvg = intentInterArrivalEma.value.toDurationOrZero(),
                interArrivalMedian = intentInterArrival.getQuantile(Q50).toDurationOrZero(),
                burstMax = intentBurstMax,
                bufferMaxOccupancy = intentBufferMaxOccupancy,
                bufferOverflows = intentOverflows,
                pluginOverheadAvg = intentPerf.averageTimeMillis.toDurationOrZero(),
                pluginOverheadMedian = intentPluginOverhead.getQuantile(Q50).toDurationOrZero(),
            ),
            actions = ActionMetrics(
                sent = actionSent,
                delivered = actionDelivered,
                undelivered = actionUndelivered,
                opsPerSecond = actionPerf.opsPerSecond(),
                deliveryAvg = actionPerf.averageTimeMillis.toDurationOrZero(),
                deliveryP50 = actionDeliveries.getQuantile(Q50).toDurationOrZero(),
                deliveryP90 = actionDeliveries.getQuantile(Q90).toDurationOrZero(),
                deliveryP95 = actionDeliveries.getQuantile(Q95).toDurationOrZero(),
                deliveryP99 = actionDeliveries.getQuantile(Q99).toDurationOrZero(),
                queueTimeAvg = actionQueueEma.value.toDurationOrZero(),
                queueTimeMedian = actionQueueMedian.getQuantile(Q50).toDurationOrZero(),
                bufferMaxOccupancy = actionBufferMaxOccupancy,
                bufferOverflows = actionOverflows,
                pluginOverheadAvg = actionPluginEma.value.toDurationOrZero(),
                pluginOverheadMedian = actionPluginOverhead.getQuantile(Q50).toDurationOrZero(),
            ),
            state = StateMetrics(
                transitions = stateTransitions,
                transitionsVetoed = stateVetoed,
                updateAvg = statePerf.averageTimeMillis.toDurationOrZero(),
                updateP50 = stateDurations.getQuantile(Q50).toDurationOrZero(),
                updateP90 = stateDurations.getQuantile(Q90).toDurationOrZero(),
                updateP95 = stateDurations.getQuantile(Q95).toDurationOrZero(),
                updateP99 = stateDurations.getQuantile(Q99).toDurationOrZero(),
                opsPerSecond = statePerf.opsPerSecond(),
            ),
            subscriptions = SubscriptionMetrics(
                events = subscriptionEvents,
                current = currentSubscribers,
                peak = peakSubscribers,
                lifetimeAvg = lifetimeEma.value.toDurationOrZero(),
                lifetimeMedian = lifetimeMedian.getQuantile(Q50).toDurationOrZero(),
                subscribersAvg = computeSubscribersAverage(),
                subscribersMedian = subscribersMedian.getQuantile(Q50).takeUnless(Double::isNaN) ?: 0.0,
            ),
            lifecycle = LifecycleMetrics(
                startCount = startCount,
                stopCount = stopCount,
                uptimeTotal = uptimeTotal,
                lifetimeCurrent = currentStart?.let { clock.now() - it } ?: ZERO,
                lifetimeAvg = lifetimeRunEma.value.toDurationOrZero(),
                lifetimeMedian = lifetimeRuns.getQuantile(Q50).toDurationOrZero(),
                bootstrapAvg = bootstrapEma.value.toDurationOrZero(),
                bootstrapMedian = bootstrapMedian.getQuantile(Q50).toDurationOrZero(),
            ),
            exceptions = ExceptionMetrics(
                total = exceptionTotal,
                handled = exceptionHandled,
                recoveryLatencyAvg = recoveryEma.value.toDurationOrZero(),
                recoveryLatencyMedian = recoveryMedian.getQuantile(Q50).toDurationOrZero(),
            ),
            storeConfiguration = storeConfiguration,
        ).also { sink.emit(it) }
    }

    /** Clears all internal counters and estimators. */
    public fun reset(): Unit = synchronized(this) {
        intentPerf.reset()
        actionPerf.reset()
        statePerf.reset()
        intentDurations.clear()
        intentPluginOverhead.clear()
        intentQueueTimes.clear()
        intentQueueEma.reset()
        intentInterArrivalEma.reset()
        intentInterArrival.clear()
        intentTotal = 0
        intentProcessed = 0
        intentDropped = 0
        intentUndelivered = 0
        intentOverflows = 0
        intentInFlight = 0
        intentInFlightMax = 0
        intentBurstSecond = null
        intentBurstCurrent = 0
        intentBurstMax = 0
        lastIntentEnqueue = null
        intentBufferMaxOccupancy = 0
        actionDeliveries.clear()
        actionQueueTimes.clear()
        actionQueueEma.reset()
        actionQueueMedian.clear()
        actionPluginOverhead.clear()
        actionPluginEma.reset()
        actionSent = 0
        actionDelivered = 0
        actionUndelivered = 0
        actionOverflows = 0
        actionBufferMaxOccupancy = 0
        actionInFlight = 0
        stateDurations.clear()
        stateTransitions = 0
        stateVetoed = 0
        subscriptionEvents = 0
        currentSubscribers = 0
        peakSubscribers = 0
        lastSubscribersChange = clock.now()
        subscribersTimeWeighted = 0.0
        subscribersTimeTotalMillis = 0.0
        subscribersMedian.clear()
        lifetimeMedian.clear()
        lifetimeEma.reset()
        startCount = 0
        stopCount = 0
        uptimeTotal = ZERO
        currentStart = null
        lifetimeRuns.clear()
        lifetimeRunEma.reset()
        bootstrapMedian.clear()
        bootstrapEma.reset()
        exceptionTotal = 0
        exceptionHandled = 0
        recoveryMedian.clear()
        recoveryEma.reset()
    }

    /** Builds a [Meta] instance using the collector defaults. */
    public fun defaultMeta(
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

    private fun updateIntentOccupancyLocked() {
        val occupancy = intentQueueTimes.size + intentInFlight
        if (occupancy > intentBufferMaxOccupancy) intentBufferMaxOccupancy = occupancy
    }

    private fun handleSubscriptionChange(newCount: Int) {
        synchronized(this) {
            val now = clock.now()
            val elapsed = now - lastSubscribersChange
            if (!elapsed.isNegative()) {
                val elapsedMillis = elapsed.inWholeMilliseconds.toDouble()
                subscribersTimeWeighted += currentSubscribers * elapsedMillis
                subscribersTimeTotalMillis += elapsedMillis
            }
            if (newCount < currentSubscribers) {
                val removed = currentSubscribers - newCount
                val durationMillis = elapsed.inWholeMilliseconds.toDouble()
                repeat(removed) {
                    lifetimeEma.add(durationMillis)
                    lifetimeMedian.add(durationMillis)
                }
            }
            subscriptionEvents++
            currentSubscribers = newCount
            if (currentSubscribers > peakSubscribers) peakSubscribers = currentSubscribers
            subscribersMedian.add(currentSubscribers.toDouble())
            lastSubscribersChange = now
        }
    }

    private fun Double.toDuration(): Duration = this.milliseconds

    private fun Double.toDurationOrZero(): Duration = when {
        isNaN() -> ZERO
        else -> toDuration()
    }

    private fun Ema.reset() {
        value = Double.NaN
        count = 0L
    }

    private fun computeSubscribersAverage(): Double {
        if (subscribersTimeTotalMillis == 0.0) return currentSubscribers.toDouble()
        return subscribersTimeWeighted / subscribersTimeTotalMillis
    }
}

private class Ema(private val alpha: Double) {
    var value: Double = Double.NaN
    var count: Long = 0

    fun add(sample: Double) {
        value = if (count == 0L) sample else alpha * sample + (1 - alpha) * value
        count++
    }
}
