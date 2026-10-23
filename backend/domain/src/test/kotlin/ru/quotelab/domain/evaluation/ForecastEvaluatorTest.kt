package ru.quotelab.domain.evaluation

import org.junit.jupiter.api.Test
import ru.quotelab.domain.TestCandles
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ForecastEvaluatorTest {
    private val threshold = BigDecimal("0.001")

    private fun forecast(direction: Direction, confidence: Double = 0.7) = Forecast.issue(
        strategyId = StrategyId.of("test", "1"),
        secid = TestCandles.SECID,
        horizon = Horizon(1),
        tradeDate = TestCandles.START,
        basePrice = Price(BigDecimal("100")),
        signal = Signal(direction, Confidence(confidence)),
        issuedAt = Instant.EPOCH,
        origin = ForecastOrigin.LIVE,
    )

    private fun evaluate(direction: Direction, close: Double, confidence: Double = 0.7) =
        ForecastEvaluator.evaluate(forecast(direction, confidence), TestCandles.candle(1, close), threshold, Instant.EPOCH)

    @Test
    fun `hit when the direction matches, brier uses confidence in the issued direction`() {
        val e = evaluate(Direction.UP, 102.0)
        assertEquals(Outcome.HIT, e.outcome)
        assertEquals(0, BigDecimal("0.02").compareTo(e.actualReturn))
        assertEquals(0, BigDecimal("0.09").compareTo(e.brier))
    }

    @Test
    fun `miss when the price moves against the forecast`() {
        val e = evaluate(Direction.UP, 98.0)
        assertEquals(Outcome.MISS, e.outcome)
        assertEquals(0, BigDecimal("0.49").compareTo(e.brier))
    }

    @Test
    fun `small moves below the flat threshold are neutral and excluded from brier`() {
        val e = evaluate(Direction.DOWN, 100.05)
        assertEquals(Outcome.NEUTRAL, e.outcome)
        assertNull(e.brier)
    }

    @Test
    fun `a flat forecast is an abstention`() {
        assertEquals(Outcome.NEUTRAL, evaluate(Direction.FLAT, 110.0).outcome)
    }

    @Test
    fun `target must be strictly after the issue date`() {
        assertFailsWith<IllegalArgumentException> {
            ForecastEvaluator.evaluate(forecast(Direction.UP), TestCandles.candle(0, 101.0), threshold, Instant.EPOCH)
        }
    }
}
