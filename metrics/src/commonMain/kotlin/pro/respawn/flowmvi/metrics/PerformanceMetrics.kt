package pro.respawn.flowmvi.metrics

import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant

internal class PerformanceMetrics(
    private val windowSeconds: Int = 60,
    private val emaAlpha: Double = 0.1,
    private val bucketDuration: Duration = 1.seconds,
    private val clock: Clock = Clock.System,
) {

    init {
        require(windowSeconds > 0) { "windowSeconds must be > 0" }
        require(emaAlpha > 0.0 && emaAlpha < 1.0) { "emaAlpha must be in (0, 1)" }
        require(bucketDuration.isPositive()) { "bucketDuration must be > 0" }
    }

    private var emaMillis: Double = 0.0

    private val p2: P2QuantileEstimator = P2QuantileEstimator(0.5)

    private var _totalOperations: Long = 0L

    private val bucketCount: Int = windowSeconds
    private val buckets: IntArray = IntArray(bucketCount)
    private var currentBucketIndex: Int = 0
    private var lastBucketTime = clock.now()

    fun recordOperation(duration: Duration) {
        val durationMillis = duration.toDouble(DurationUnit.MILLISECONDS)
        _totalOperations++
        emaMillis = if (_totalOperations == 1L) {
            durationMillis
        } else {
            emaAlpha * durationMillis + (1.0 - emaAlpha) * emaMillis
        }

        advanceBuckets()
        buckets[currentBucketIndex]++
        p2.add(durationMillis)
    }

    private fun advanceBuckets() {
        val now = clock.now()
        val elapsed = now - lastBucketTime
        val elapsedBuckets = (elapsed / bucketDuration).toInt()

        if (elapsedBuckets <= 0) return

        val steps = min(elapsedBuckets, bucketCount)
        repeat(steps) {
            currentBucketIndex = (currentBucketIndex + 1) % bucketCount
            buckets[currentBucketIndex] = 0
        }

        lastBucketTime += bucketDuration * elapsedBuckets
    }

    val totalOperations: Long
        get() = _totalOperations

    val averageTimeMillis: Double
        get() = if (_totalOperations == 0L) Double.NaN else emaMillis

    suspend fun medianTimeMillis(): Double = p2.getQuantile(0.5)

    fun opsPerSecond(): Double {
        advanceBuckets()
        val windowOps = buckets.sum()
        val windowDurationSeconds = (bucketDuration * bucketCount).toDouble(DurationUnit.SECONDS)
        return windowOps.toDouble() / windowDurationSeconds
    }

    fun reset() {
        _totalOperations = 0L
        emaMillis = 0.0
        buckets.fill(0)
        currentBucketIndex = 0
        lastBucketTime = clock.now()
        p2.clear()
    }

    /**
     * Internal function to snapshot state for testing.
     */
    internal fun stateForTest(): PerformanceMetricsState = PerformanceMetricsState(
        bucketIndex = currentBucketIndex,
        buckets = buckets.copyOf(),
        lastBucketTime = lastBucketTime,
        totalOperations = _totalOperations,
        emaMillis = emaMillis,
    )
}

internal data class PerformanceMetricsState(
    val bucketIndex: Int,
    val buckets: IntArray,
    val lastBucketTime: Instant,
    val totalOperations: Long,
    val emaMillis: Double,
)
