package pro.respawn.flowmvi.metrics

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sign

/**
 * P2quantile algorithm implementation to estimate median values.
 * Credit: https://aakinshin.net/posts/ex-p2-quantile-estimator/
 * Accessed on 2024-11-27
 */
internal class P2QuantileEstimator(private vararg val probabilities: Double) : SynchronizedObject() {

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
                for (i in 0 until markerCount) {
                    n[i] = round(ns[i]).toInt()
                }
                q.copyInto(ns)
                for (i in 0 until markerCount) {
                    q[i] = ns[n[i]]
                }
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

        for (i in k + 1 until markerCount) {
            n[i]++
        }
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
        // Principal markers
        ns[0] = 0.0
        for (i in 0 until m) {
            ns[i * 2 + 2] = maxIndex * probabilities[i]
        }
        ns[markerCount - 1] = maxIndex.toDouble()

        // Middle markers
        ns[1] = maxIndex * probabilities[0] / 2.0
        for (i in 1 until m) {
            ns[2 * i + 1] = maxIndex * (probabilities[i - 1] + probabilities[i]) / 2.0
        }
        ns[markerCount - 2] = maxIndex * (1 + probabilities[m - 1]) / 2.0
    }

    private fun adjust(i: Int) {
        val d = ns[i] - n[i]
        if (d >= 1 && n[i + 1] - n[i] > 1 || d <= -1 && n[i - 1] - n[i] < -1) {
            val dInt = d.sign.toInt()
            val qs = parabolic(i, dInt.toDouble())
            if (q[i - 1] < qs && qs < q[i + 1]) {
                q[i] = qs
            } else {
                q[i] = linear(i, dInt)
            }
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

        val numerator = (nI - nMinus1 + d) * (qPlus1 - qI) / (nPlus1 - nI) +
                (nPlus1 - nI - d) * (qI - qMinus1) / (nI - nMinus1)

        return qI + d / (nPlus1 - nMinus1) * numerator
    }

    private fun linear(i: Int, d: Int) = q[i] + d * (q[i + d] - q[i]) / (n[i + d] - n[i]).toDouble()

    fun getQuantile(p: Double): Double = synchronized(this) {
        if (count == 0) return Double.NaN
        if (count <= markerCount) {
            q.sort(0, count)
            val index = round((count - 1) * p).toInt()
            return q[index]
        }

        for (i in probabilities.indices) {
            if (probabilities[i] == p)
                return q[2 * i + 2]
        }

        throw IllegalStateException("Target quantile ($p) wasn't requested in the constructor")
    }

    fun clear() {
        count = 0
    }
}
