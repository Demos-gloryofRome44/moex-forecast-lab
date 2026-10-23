package ru.quotelab.domain.evaluation

import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.forecast.valueobject.ForecastId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class Evaluation(
    val forecastId: ForecastId,
    val evaluatedAt: Instant,
    val targetTradeDate: LocalDate,
    val actualPrice: Price,
    val actualReturn: BigDecimal,
    val outcome: Outcome,
    val brier: BigDecimal?,
)
