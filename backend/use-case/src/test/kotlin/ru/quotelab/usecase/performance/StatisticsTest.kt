package ru.quotelab.usecase.performance

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatisticsTest {
    @Test
    fun `wilson interval matches the textbook value`() {
        val interval = Statistics.wilson(50, 100, 1.96)
        assertEquals(0.4038, interval.low, 1e-4)
        assertEquals(0.5962, interval.high, 1e-4)
    }

    @Test
    fun `wilson interval stays inside 0 and 1 at the edges`() {
        assertEquals(0.0, Statistics.wilson(0, 10, 1.96).low)
        assertEquals(1.0, Statistics.wilson(10, 10, 1.96).high, 1e-12)
        assertEquals(Interval(0.0, 1.0), Statistics.wilson(0, 0, 1.96))
    }

    @Test
    fun `max drawdown is measured from the running peak`() {
        val equity = listOf("1", "1.2", "0.9", "1.1", "0.6", "1.3").map(::BigDecimal)
        assertEquals(0.5, Statistics.maxDrawdown(equity).toDouble(), 1e-12)
    }

    @Test
    fun `sharpe needs variance`() {
        assertNull(Statistics.sharpe(listOf(0.01, 0.01, 0.01), 252.0))
        assertEquals(0.0, Statistics.sharpe(listOf(0.01, -0.01), 252.0)!!, 1e-12)
    }
}
