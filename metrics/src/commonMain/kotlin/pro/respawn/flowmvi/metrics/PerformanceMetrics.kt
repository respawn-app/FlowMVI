package pro.respawn.flowmvi.metrics

import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

internal class PerformanceMetrics(
    private val windowSeconds: Int = 60,
    private val emaAlpha: Double = 0.1,
    private val bucketDuration: Duration = 1.seconds,
) {

    init {
        require(windowSeconds > 0) { "windowSeconds must be > 0" }
        require(bucketDuration.isPositive()) { "bucketDuration must be > 0" }
    }

    private var emaMillis: Double = 0.0

    private val p2: P2QuantileEstimator = P2QuantileEstimator(0.5)

    private var _totalOperations: Long = 0L

    private val bucketCount: Int = windowSeconds
    private val buckets: IntArray = IntArray(bucketCount)
    private var currentBucketIndex: Int = 0
    private var lastBucketTime = Clock.System.now()

    suspend fun recordOperation(duration: Duration) {
        val duration = duration.toDouble(DurationUnit.MILLISECONDS)
        _totalOperations++
        emaMillis = if (_totalOperations == 1L) {
            duration
        } else {
            emaAlpha * duration + (1.0 - emaAlpha) * emaMillis
        }

        advanceBuckets()
        buckets[currentBucketIndex]++
        p2.add(duration)
    }

    private fun advanceBuckets() {
        val now = Clock.System.now()
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
        return windowOps.toDouble() / bucketCount.toDouble()
    }

    fun reset() {
        _totalOperations = 0L
        emaMillis = 0.0
        buckets.fill(0)
        currentBucketIndex = 0
        lastBucketTime = Clock.System.now()
        p2.clear()
    }
}
