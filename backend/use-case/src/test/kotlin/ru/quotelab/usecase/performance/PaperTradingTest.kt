package ru.quotelab.usecase.performance

import org.junit.jupiter.api.Test
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.evaluation.EvaluatedForecast
import ru.quotelab.domain.evaluation.Evaluation
import ru.quotelab.domain.evaluation.Outcome
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.usecase.common.PaperTradingSettings
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals

class PaperTradingTest {
    private val paper = PaperTrading(PaperTradingSettings(BigDecimal("0.0005"), BigDecimal("0.0005")))

    private fun item(direction: Direction, actualReturn: String, horizon: Int = 1, day: Long = 1): EvaluatedForecast {
        val date = LocalDate.of(2026, 1, 1)
        val forecast = Forecast.issue(
            StrategyId.of("t", "1"), Secid("SBER"), Horizon(horizon), date, Price(BigDecimal("100")),
            Signal(direction, Confidence(0.6)), Instant.EPOCH, ForecastOrigin.LIVE,
        )
        val evaluation = Evaluation(
            forecast.id, Instant.EPOCH, date.plusDays(day), Price(BigDecimal("101")), BigDecimal(actualReturn), Outcome.HIT, null,
        )
        return EvaluatedForecast(forecast, evaluation)
    }

    @Test
    fun `long and short pay a round-trip cost, flat stays in cash`() {
        assertEquals(0, BigDecimal("0.018").compareTo(paper.netReturn(item(Direction.UP, "0.02"))))
        assertEquals(0, BigDecimal("-0.022").compareTo(paper.netReturn(item(Direction.DOWN, "0.02"))))
        assertEquals(0, BigDecimal.ZERO.compareTo(paper.netReturn(item(Direction.FLAT, "0.02"))))
    }

    @Test
    fun `each forecast is a 1 over h tranche, so overlapping forecasts do not add leverage`() {
        val curve = paper.equityCurve(listOf(item(Direction.UP, "0.102", horizon = 5)))
        assertEquals(0, BigDecimal("1.02").compareTo(curve.single().equity))
    }

    @Test
    fun `stats count only non-flat trades`() {
        val stats = paper.stats(listOf(item(Direction.UP, "0.01", day = 1), item(Direction.FLAT, "0.01", day = 2)))
        assertEquals(1, stats.trades)
    }
}
