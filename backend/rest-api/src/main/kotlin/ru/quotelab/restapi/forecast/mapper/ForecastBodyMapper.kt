package ru.quotelab.restapi.forecast.mapper

import ru.quotelab.restapi.forecast.dto.EvaluationBody
import ru.quotelab.restapi.forecast.dto.ForecastBody
import ru.quotelab.restapi.forecast.dto.ForecastPageBody
import ru.quotelab.usecase.forecast.dto.EvaluationResponse
import ru.quotelab.usecase.forecast.dto.ForecastPageResponse
import ru.quotelab.usecase.forecast.dto.ForecastResponse

internal fun ForecastResponse.toBody() = ForecastBody(
    id = id,
    strategyId = strategyId,
    strategyCode = strategyCode,
    strategyVersion = strategyVersion,
    secid = secid,
    horizon = horizon,
    issuedAt = issuedAt,
    tradeDate = tradeDate,
    basePrice = basePrice,
    direction = direction,
    confidence = confidence,
    expectedTargetDate = expectedTargetDate,
    status = status,
    origin = origin,
    evaluation = evaluation?.toBody(),
)

internal fun EvaluationResponse.toBody() = EvaluationBody(
    evaluatedAt = evaluatedAt,
    targetTradeDate = targetTradeDate,
    actualPrice = actualPrice,
    actualReturn = actualReturn,
    outcome = outcome,
    brier = brier,
)

internal fun ForecastPageResponse.toBody() = ForecastPageBody(
    items = items.map { it.toBody() },
    page = page,
    size = size,
    total = total,
)
