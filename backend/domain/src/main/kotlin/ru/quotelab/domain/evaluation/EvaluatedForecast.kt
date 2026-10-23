package ru.quotelab.domain.evaluation

import ru.quotelab.domain.forecast.Forecast

data class EvaluatedForecast(
    val forecast: Forecast,
    val evaluation: Evaluation,
)
