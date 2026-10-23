package ru.quotelab.domain.strategy

import org.junit.jupiter.api.Test
import ru.quotelab.domain.TestCandles
import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.strategy.baseline.LastDirectionBaseline
import ru.quotelab.domain.strategy.baseline.RandomBaseline
import ru.quotelab.domain.strategy.indicatorbased.BollingerStrategy
import ru.quotelab.domain.strategy.indicatorbased.RsiStrategy
import ru.quotelab.domain.strategy.indicatorbased.SmaCrossoverStrategy
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StrategiesTest {
    private fun seriesOf(closes: List<Double>) =
        CandleSeries.asOf(TestCandles.SECID, TestCandles.START.plusDays(closes.size - 1L), TestCandles.series(closes))

    private fun signal(strategy: Strategy, closes: List<Double>): Signal =
        (strategy.signal(seriesOf(closes), Horizon(1)) as StrategyResult.Success).signal

    @Test
    fun `sma crossover follows the trend`() {
        val up = (1..40).map { 100.0 + it }
        val down = up.reversed()
        assertEquals(Direction.UP, signal(SmaCrossoverStrategy("1", 5, 20), up).direction)
        assertEquals(Direction.DOWN, signal(SmaCrossoverStrategy("1", 5, 20), down).direction)
        assertTrue(signal(SmaCrossoverStrategy("1", 5, 20), up).confidence.value > 0.5)
    }

    @Test
    fun `rsi bets on mean reversion after a sell-off`() {
        val selloff = List(30) { 100.0 } + (1..15).map { 100.0 - it * 2 }
        assertEquals(Direction.UP, signal(RsiStrategy("1"), selloff).direction)
        val rally = List(30) { 100.0 } + (1..15).map { 100.0 + it * 2 }
        assertEquals(Direction.DOWN, signal(RsiStrategy("1"), rally).direction)
    }

    @Test
    fun `bollinger is flat inside the bands and fades a breakout`() {
        val calm = List(25) { i -> 100.0 + if (i % 2 == 0) 1 else -1 }
        assertEquals(Direction.FLAT, signal(BollingerStrategy("1"), calm).direction)
        assertEquals(Direction.DOWN, signal(BollingerStrategy("1"), calm + 120.0).direction)
    }

    @Test
    fun `last direction repeats the previous move`() {
        assertEquals(Direction.UP, signal(LastDirectionBaseline(), listOf(1.0, 2.0)).direction)
        assertEquals(Direction.DOWN, signal(LastDirectionBaseline(), listOf(2.0, 1.0)).direction)
    }

    @Test
    fun `random baseline is reproducible and depends on the seed`() {
        val closes = (1..60).map { it.toDouble() }
        val a = (1..30).map { signal(RandomBaseline("1", 42), closes.take(it + 1)) }
        val b = (1..30).map { signal(RandomBaseline("1", 42), closes.take(it + 1)) }
        val c = (1..30).map { signal(RandomBaseline("1", 43), closes.take(it + 1)) }
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `new parameters require a new version, the id is derived from code and version`() {
        assertEquals(SmaCrossoverStrategy("1", 5, 20).id, StrategyId.of(SmaCrossoverStrategy.CODE, "1"))
        assertNotEquals(SmaCrossoverStrategy("1").id, SmaCrossoverStrategy("2").id)
        assertNotEquals(SmaCrossoverStrategy("1", 5, 20).descriptor.params, SmaCrossoverStrategy("1", 10, 30).descriptor.params)
    }
}
