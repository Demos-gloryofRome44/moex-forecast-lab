package ru.quotelab.usecase.forecast.mapper

import ru.quotelab.domain.evaluation.Evaluation
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.usecase.forecast.dto.EvaluationResponse
import ru.quotelab.usecase.forecast.dto.ForecastResponse

internal fun Forecast.toResponse(strategy: StrategyDescriptor?, evaluation: Evaluation?) = ForecastResponse(
    id = id.value,
    strategyId = strategyId.value,
    strategyCode = strategy?.code,
    strategyVersion = strategy?.version,
    secid = secid.value,
    horizon = horizon.days,
    issuedAt = issuedAt,
    tradeDate = tradeDate,
    basePrice = basePrice.value,
    direction = direction.name,
    confidence = confidence.value,
    expectedTargetDate = expectedTargetDate,
    status = status.name,
    origin = origin.name,
    evaluation = evaluation?.toResponse(),
)

internal fun Evaluation.toResponse() = EvaluationResponse(
    evaluatedAt = evaluatedAt,
    targetTradeDate = targetTradeDate,
    actualPrice = actualPrice.value,
    actualReturn = actualReturn,
    outcome = outcome.name,
    brier = brier,
)
