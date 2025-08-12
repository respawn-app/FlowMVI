package pro.respawn.flowmvi.metrics

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

internal class PerformanceMetrics : SynchronizedObject() {

    // For Average Time using EMA
    private var ema: Double = 0.0
    private val alpha: Double = 0.1 // Smoothing factor

    // For Median Time using P² Algorithm
    private var p2: P2QuantileEstimator = P2QuantileEstimator(0.5) // Median

    var totalOperations: Long = 0
        private set

    private val numberOfBuckets: Int = 60 // Last 60 seconds
    private val frequencyBuckets = IntArray(numberOfBuckets)
    private val bucketDurationMillis = 1.seconds // 1 second per bucket
    private var lastBucketTime = Clock.System.now()

    fun recordOperation(durationMillis: Long) = synchronized(this) {
        totalOperations++
        ema = if (ema == 0.0) durationMillis.toDouble() else alpha * durationMillis + (1 - alpha) * ema
        p2.add(durationMillis.toDouble())
        updateFrequencyCounter()
    }

    private fun updateFrequencyCounter() = synchronized(this) {
        val currentTime = Clock.System.now()
        val elapsedBuckets = ((currentTime - lastBucketTime) / bucketDurationMillis).toInt()

        if (elapsedBuckets > 0) {
            val shift = min(elapsedBuckets, numberOfBuckets)
            frequencyBuckets.copyInto(frequencyBuckets, shift, 0, numberOfBuckets - shift)
            for (i in numberOfBuckets - shift until numberOfBuckets) {
                frequencyBuckets[i] = 0
            }
            lastBucketTime += bucketDurationMillis * elapsedBuckets
        }

        frequencyBuckets[numberOfBuckets - 1]++
    }

    val averageTime get() = ema

    fun medianTime(q: Double): Double = p2.getQuantile(q)

    fun opsPerSecond(): Double = synchronized(this) {
        return frequencyBuckets.sum().toDouble() / numberOfBuckets
    }
}
