package ru.quotelab.usecase.performance

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.sqrt

data class Interval(val low: Double, val high: Double)

internal object Statistics {
    fun wilson(successes: Int, n: Int, z: Double): Interval {
        if (n == 0) return Interval(0.0, 1.0)
        val p = successes.toDouble() / n
        val z2 = z * z
        val denominator = 1 + z2 / n
        val center = (p + z2 / (2 * n)) / denominator
        val margin = z * sqrt(p * (1 - p) / n + z2 / (4.0 * n * n)) / denominator
        return Interval((center - margin).coerceAtLeast(0.0), (center + margin).coerceAtMost(1.0))
    }

    fun mean(values: List<Double>): Double? = if (values.isEmpty()) null else values.sum() / values.size

    fun stdDev(values: List<Double>): Double? {
        if (values.size < 2) return null
        val m = values.sum() / values.size
        return sqrt(values.sumOf { (it - m) * (it - m) } / (values.size - 1))
    }

    fun sharpe(returns: List<Double>, periodsPerYear: Double): Double? {
        val m = mean(returns) ?: return null
        val s = stdDev(returns) ?: return null
        if (s == 0.0) return null
        return m / s * sqrt(periodsPerYear)
    }

    fun maxDrawdown(equity: List<BigDecimal>): BigDecimal {
        var peak = BigDecimal.ZERO
        var worst = BigDecimal.ZERO
        for (value in equity) {
            if (value > peak) peak = value
            if (peak.signum() > 0) {
                val dd = (peak - value).divide(peak, MathContext.DECIMAL64)
                if (dd > worst) worst = dd
            }
        }
        return worst
    }
}
