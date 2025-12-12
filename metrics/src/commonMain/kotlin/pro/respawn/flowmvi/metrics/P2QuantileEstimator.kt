package pro.respawn.flowmvi.metrics

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sign

internal class P2QuantileEstimator(
    vararg requestedProbabilities: Double,
) : SynchronizedObject() {

    private val probabilities: DoubleArray = requestedProbabilities.copyOf().apply {
        sort()
        require(isNotEmpty()) { "At least one probability must be provided" }
        for (i in indices) {
            val p = this[i]
            require(p > 0.0 && p < 1.0) { "Probability $p is out of (0, 1) range" }
            if (i > 0) require(this[i - 1] < p) { "Probabilities must be strictly increasing" }
        }
    }

    private val probabilityToMarkerIndex: Map<Double, Int> =
        probabilities.mapIndexed { i, p -> p to 2 * i + 2 }.toMap()

    private val m: Int = probabilities.size
    private val markerCount: Int = 2 * m + 3
    private val n: IntArray = IntArray(markerCount)
    private val ns: DoubleArray = DoubleArray(markerCount)
    private val q: DoubleArray = DoubleArray(markerCount)

    var count: Int = 0
        private set

    fun add(value: Double) = synchronized(this) {
        if (count < markerCount) {
            q[count++] = value
            if (count == markerCount) {
                q.sort()

                updateNs(markerCount - 1)
                for (i in 0 until markerCount) n[i] = round(ns[i]).toInt()
                q.copyInto(ns)
                for (i in 0 until markerCount) q[i] = ns[n[i]]
                updateNs(markerCount - 1)
            }
            return@synchronized
        }

        var k = -1
        if (value < q[0]) {
            q[0] = value
            k = 0
        } else {
            for (i in 1 until markerCount) {
                if (value < q[i]) {
                    k = i - 1
                    break
                }
            }
            if (k == -1) {
                q[markerCount - 1] = value
                k = markerCount - 2
            }
        }

        for (i in k + 1 until markerCount) n[i]++
        updateNs(count)

        var leftI = 1
        var rightI = markerCount - 2
        while (leftI <= rightI) {
            val i: Int
            if (abs(ns[leftI] / count - 0.5) <= abs(ns[rightI] / count - 0.5)) {
                i = leftI
                leftI++
            } else {
                i = rightI
                rightI--
            }
            adjust(i)
        }

        count++
    }

    private fun updateNs(maxIndex: Int) {
        ns[0] = 0.0
        for (i in 0 until m) ns[i * 2 + 2] = maxIndex * probabilities[i]
        ns[markerCount - 1] = maxIndex.toDouble()

        ns[1] = maxIndex * probabilities[0] / 2.0
        for (i in 1 until m) ns[2 * i + 1] = maxIndex * (probabilities[i - 1] + probabilities[i]) / 2.0
        ns[markerCount - 2] = maxIndex * (1 + probabilities[m - 1]) / 2.0
    }

    private fun adjust(i: Int) {
        val d = ns[i] - n[i]
        if (d >= 1 && n[i + 1] - n[i] > 1 || d <= -1 && n[i - 1] - n[i] < -1) {
            val dInt = d.sign.toInt()
            val qs = parabolic(i, dInt.toDouble())
            q[i] = if (q[i - 1] < qs && qs < q[i + 1]) qs else linear(i, dInt)
            n[i] += dInt
        }
    }

    private fun parabolic(i: Int, d: Double): Double {
        val nPlus1 = n[i + 1].toDouble()
        val nMinus1 = n[i - 1].toDouble()
        val nI = n[i].toDouble()

        val qPlus1 = q[i + 1]
        val qMinus1 = q[i - 1]
        val qI = q[i]

        val numerator =
            (nI - nMinus1 + d) * (qPlus1 - qI) / (nPlus1 - nI) +
                (nPlus1 - nI - d) * (qI - qMinus1) / (nI - nMinus1)

        return qI + d / (nPlus1 - nMinus1) * numerator
    }

    private fun linear(i: Int, d: Int): Double =
        q[i] + d * (q[i + d] - q[i]) / (n[i + d] - n[i]).toDouble()

    fun getQuantile(p: Double): Double = synchronized(this) {
        if (count == 0) return Double.NaN

        if (count <= markerCount) {
            q.sort(0, count)
            val index = round((count - 1) * p).toInt().coerceIn(0, count - 1)
            return q[index]
        }

        val markerIndex = probabilityToMarkerIndex[p]
            ?: throw IllegalArgumentException("Target quantile ($p) wasn't requested in the constructor")

        return q[markerIndex]
    }

    fun clear() = synchronized(this) {
        count = 0
        n.fill(0)
        ns.fill(0.0)
        q.fill(0.0)
    }
}
