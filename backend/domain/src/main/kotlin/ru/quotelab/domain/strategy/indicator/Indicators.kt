package ru.quotelab.domain.strategy.indicator

import java.math.BigDecimal
import java.math.MathContext

object Indicators {
    val MC: MathContext = MathContext.DECIMAL64
    private val TWO = BigDecimal(2)
    private val HUNDRED = BigDecimal(100)

    fun sma(values: List<BigDecimal>, period: Int): List<BigDecimal?> {
        requirePeriod(period)
        val result = MutableList<BigDecimal?>(values.size) { null }
        var window = BigDecimal.ZERO
        values.forEachIndexed { i, v ->
            window += v
            if (i >= period) window -= values[i - period]
            if (i >= period - 1) result[i] = window.divide(BigDecimal(period), MC)
        }
        return result
    }

    fun ema(values: List<BigDecimal>, period: Int): List<BigDecimal?> {
        requirePeriod(period)
        val result = MutableList<BigDecimal?>(values.size) { null }
        if (values.size < period) return result
        val alpha = TWO.divide(BigDecimal(period + 1), MC)
        var prev = values.subList(0, period).fold(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal(period), MC)
        result[period - 1] = prev
        for (i in period until values.size) {
            prev = (values[i] - prev).multiply(alpha, MC) + prev
            result[i] = prev
        }
        return result
    }

    fun rsi(values: List<BigDecimal>, period: Int): List<BigDecimal?> {
        requirePeriod(period)
        val result = MutableList<BigDecimal?>(values.size) { null }
        if (values.size <= period) return result
        var avgGain = BigDecimal.ZERO
        var avgLoss = BigDecimal.ZERO
        for (i in 1..period) {
            val change = values[i] - values[i - 1]
            if (change.signum() > 0) avgGain += change else avgLoss -= change
        }
        val n = BigDecimal(period)
        avgGain = avgGain.divide(n, MC)
        avgLoss = avgLoss.divide(n, MC)
        result[period] = rsiValue(avgGain, avgLoss)
        val nMinusOne = BigDecimal(period - 1)
        for (i in period + 1 until values.size) {
            val change = values[i] - values[i - 1]
            val gain = change.max(BigDecimal.ZERO)
            val loss = change.negate().max(BigDecimal.ZERO)
            avgGain = (avgGain.multiply(nMinusOne) + gain).divide(n, MC)
            avgLoss = (avgLoss.multiply(nMinusOne) + loss).divide(n, MC)
            result[i] = rsiValue(avgGain, avgLoss)
        }
        return result
    }

    fun macd(values: List<BigDecimal>, fast: Int, slow: Int, signal: Int): List<MacdPoint?> {
        require(fast < slow) { "MACD fast period must be less than slow period" }
        requirePeriod(signal)
        val fastEma = ema(values, fast)
        val slowEma = ema(values, slow)
        val macdLine = values.indices.map { i ->
            val f = fastEma[i]
            val s = slowEma[i]
            if (f != null && s != null) f - s else null
        }
        val firstDefined = macdLine.indexOfFirst { it != null }
        val result = MutableList<MacdPoint?>(values.size) { null }
        if (firstDefined < 0) return result
        val signalLine = ema(macdLine.subList(firstDefined, macdLine.size).map { it!! }, signal)
        signalLine.forEachIndexed { j, sig ->
            if (sig != null) {
                val m = macdLine[firstDefined + j]!!
                result[firstDefined + j] = MacdPoint(macd = m, signal = sig, histogram = m - sig)
            }
        }
        return result
    }

    fun bollinger(values: List<BigDecimal>, period: Int, k: BigDecimal): List<BollingerBand?> {
        val middle = sma(values, period)
        return values.indices.map { i ->
            val mid = middle[i] ?: return@map null
            val variance = (i - period + 1..i)
                .fold(BigDecimal.ZERO) { acc, j -> acc + (values[j] - mid).pow(2, MC) }
                .divide(BigDecimal(period), MC)
            val width = variance.sqrt(MC).multiply(k, MC)
            BollingerBand(lower = mid - width, middle = mid, upper = mid + width)
        }
    }

    private fun rsiValue(avgGain: BigDecimal, avgLoss: BigDecimal): BigDecimal {
        if (avgLoss.signum() == 0) return if (avgGain.signum() == 0) BigDecimal(50) else HUNDRED
        val rs = avgGain.divide(avgLoss, MC)
        return HUNDRED - HUNDRED.divide(BigDecimal.ONE + rs, MC)
    }

    private fun requirePeriod(period: Int) = require(period > 0) { "Period must be positive, was $period" }
}

data class MacdPoint(val macd: BigDecimal, val signal: BigDecimal, val histogram: BigDecimal)

data class BollingerBand(val lower: BigDecimal, val middle: BigDecimal, val upper: BigDecimal)
