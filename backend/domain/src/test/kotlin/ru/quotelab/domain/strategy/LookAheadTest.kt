package ru.quotelab.domain.strategy

import org.junit.jupiter.api.Test
import ru.quotelab.domain.TestCandles
import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.strategy.baseline.AlwaysUpBaseline
import ru.quotelab.domain.strategy.baseline.LastDirectionBaseline
import ru.quotelab.domain.strategy.baseline.RandomBaseline
import ru.quotelab.domain.strategy.indicatorbased.BollingerStrategy
import ru.quotelab.domain.strategy.indicatorbased.MacdStrategy
import ru.quotelab.domain.strategy.indicatorbased.RsiStrategy
import ru.quotelab.domain.strategy.indicatorbased.SmaCrossoverStrategy
import ru.quotelab.domain.strategy.valueobject.Horizon
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LookAheadTest {
    private val strategies = listOf(
        AlwaysUpBaseline(), LastDirectionBaseline(), RandomBaseline(),
        SmaCrossoverStrategy("1"), RsiStrategy("1"), MacdStrategy("1"), BollingerStrategy("1"),
    )

    @Test
    fun `future candles never change a signal`() {
        val past = TestCandles.randomWalk(200)
        val asOf = past.last().tradeDate
        val future = (200 until 260).map { TestCandles.candle(it, 10_000.0 + it) }

        for (strategy in strategies) {
            for (horizon in listOf(Horizon(1), Horizon(5), Horizon(20))) {
                val honest = strategy.signal(CandleSeries.asOf(TestCandles.SECID, asOf, past), horizon)
                val leaked = strategy.signal(CandleSeries.asOf(TestCandles.SECID, asOf, past + future), horizon)
                assertEquals(honest, leaked, "${strategy.descriptor.code} changed its signal when future candles were supplied")
            }
        }
    }

    @Test
    fun `series as of T contains nothing after T, in date order, for that instrument only`() {
        val history = TestCandles.randomWalk(50).shuffled(java.util.Random(1)) +
            TestCandles.candle(10, 1.0, ru.quotelab.domain.instrument.valueobject.Secid("GAZP"))
        val asOf = TestCandles.START.plusDays(29)
        val series = CandleSeries.asOf(TestCandles.SECID, asOf, history)
        assertEquals(30, series.size)
        assertEquals(asOf, series.last.tradeDate)
        assertTrue(series.toList().zipWithNext().all { (a, b) -> a.tradeDate < b.tradeDate })
        assertTrue(series.toList().all { it.secid == TestCandles.SECID })
    }

    @Test
    fun `strategies report insufficient data instead of throwing`() {
        val short = CandleSeries.asOf(TestCandles.SECID, TestCandles.START.plusDays(4), TestCandles.randomWalk(5))
        for (strategy in strategies.filter { it.requiredHistory > 5 }) {
            val result = strategy.signal(short, Horizon(1))
            assertTrue(result is StrategyResult.InsufficientData, "${strategy.descriptor.code} should need more history")
        }
    }
}
