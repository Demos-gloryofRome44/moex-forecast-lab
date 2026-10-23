package ru.quotelab.domain.strategy

import org.junit.jupiter.api.Test
import ru.quotelab.domain.strategy.indicator.Indicators
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IndicatorsTest {
    private fun d(vararg values: Double) = values.map { BigDecimal(it.toString()) }

    private fun assertClose(expected: Double, actual: BigDecimal?, eps: Double = 1e-9) {
        requireNotNull(actual)
        assertTrue(kotlin.math.abs(expected - actual.toDouble()) < eps, "expected $expected, was $actual")
    }

    @Test
    fun `sma is aligned with input and undefined during warmup`() {
        val sma = Indicators.sma(d(1.0, 2.0, 3.0, 4.0, 5.0), 3)
        assertNull(sma[0])
        assertNull(sma[1])
        assertClose(2.0, sma[2])
        assertClose(3.0, sma[3])
        assertClose(4.0, sma[4])
    }

    @Test
    fun `ema starts from sma and follows the recursive formula`() {
        val ema = Indicators.ema(d(1.0, 2.0, 3.0, 4.0, 5.0), 3)
        assertNull(ema[1])
        assertClose(2.0, ema[2])
        assertClose(3.0, ema[3])
        assertClose(4.0, ema[4])
    }

    @Test
    fun `rsi is 100 for a strictly rising series and 0 for a falling one`() {
        val rising = (1..30).map { it.toDouble() }
        assertClose(100.0, Indicators.rsi(d(*rising.toDoubleArray()), 14).last())
        val falling = rising.reversed()
        assertClose(0.0, Indicators.rsi(d(*falling.toDoubleArray()), 14).last())
    }

    @Test
    fun `rsi of a flat series is neutral`() {
        assertEquals(0, BigDecimal(50).compareTo(Indicators.rsi(d(*DoubleArray(20) { 10.0 }), 14).last()))
    }

    @Test
    fun `bollinger bands collapse on a constant series`() {
        val band = Indicators.bollinger(d(*DoubleArray(25) { 7.0 }), 20, BigDecimal(2)).last()!!
        assertClose(7.0, band.lower)
        assertClose(7.0, band.middle)
        assertClose(7.0, band.upper)
    }

    @Test
    fun `bollinger width uses population standard deviation`() {
        val band = Indicators.bollinger(d(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0), 8, BigDecimal(2)).last()!!
        assertClose(1.0, band.lower)
        assertClose(9.0, band.upper)
    }

    @Test
    fun `macd of a constant series is zero`() {
        val point = Indicators.macd(d(*DoubleArray(60) { 3.0 }), 12, 26, 9).last()!!
        assertClose(0.0, point.macd)
        assertClose(0.0, point.histogram)
    }
}
