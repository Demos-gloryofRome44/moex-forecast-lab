package ru.quotelab.domain.evaluation

import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.strategy.Direction
import java.math.BigDecimal
import java.math.MathContext
import java.time.Instant

object ForecastEvaluator {
    fun evaluate(forecast: Forecast, targetCandle: Candle, flatThreshold: BigDecimal, now: Instant): Evaluation {
        require(targetCandle.secid == forecast.secid) { "Target candle belongs to another instrument" }
        require(targetCandle.tradeDate.isAfter(forecast.tradeDate)) { "Target candle must be after the issue date" }

        val actualReturn = targetCandle.close.returnFrom(forecast.basePrice)
        val actualDirection = if (actualReturn.abs() < flatThreshold) Direction.FLAT else Direction.ofSign(actualReturn.signum())
        val outcome = when {
            actualDirection == Direction.FLAT || forecast.direction == Direction.FLAT -> Outcome.NEUTRAL
            actualDirection == forecast.direction -> Outcome.HIT
            else -> Outcome.MISS
        }
        val brier = when (outcome) {
            Outcome.NEUTRAL -> null
            else -> {
                val p = BigDecimal(forecast.confidence.value.toString())
                val y = if (outcome == Outcome.HIT) BigDecimal.ONE else BigDecimal.ZERO
                (p - y).pow(2, MathContext.DECIMAL64)
            }
        }
        return Evaluation(
            forecastId = forecast.id,
            evaluatedAt = now,
            targetTradeDate = targetCandle.tradeDate,
            actualPrice = targetCandle.close,
            actualReturn = actualReturn,
            outcome = outcome,
            brier = brier,
        )
    }
}
