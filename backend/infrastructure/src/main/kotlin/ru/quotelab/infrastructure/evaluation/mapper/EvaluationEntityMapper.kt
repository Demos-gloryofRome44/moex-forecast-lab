package ru.quotelab.infrastructure.evaluation.mapper

import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.evaluation.EvaluatedForecast
import ru.quotelab.domain.evaluation.Evaluation
import ru.quotelab.domain.evaluation.Outcome
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.infrastructure.common.toUtc
import ru.quotelab.infrastructure.evaluation.EvaluatedRow
import ru.quotelab.infrastructure.evaluation.EvaluationEntity
import ru.quotelab.infrastructure.forecast.ForecastEntity
import ru.quotelab.infrastructure.forecast.mapper.toDomain

internal fun EvaluationEntity.toDomain() = Evaluation(
    forecastId = ForecastId(forecastId),
    evaluatedAt = evaluatedAt.toInstant(),
    targetTradeDate = targetTradeDate,
    actualPrice = Price(actualPrice),
    actualReturn = actualReturn,
    outcome = Outcome.valueOf(outcome),
    brier = brier,
)

internal fun Evaluation.toEntity() = EvaluationEntity(
    forecastId = forecastId.value,
    evaluatedAt = evaluatedAt.toUtc(),
    targetTradeDate = targetTradeDate,
    actualPrice = actualPrice.value,
    actualReturn = actualReturn,
    outcome = outcome.name,
    brier = brier,
)

internal fun EvaluatedRow.toDomain() = EvaluatedForecast(
    forecast = ForecastEntity(
        id, strategyId, secid, horizonDays, issuedAt, tradeDate, basePrice,
        direction, confidence, expectedTargetDate, status, origin,
    ).toDomain(),
    evaluation = EvaluationEntity(id, evaluatedAt, targetTradeDate, actualPrice, actualReturn, outcome, brier).toDomain(),
)
