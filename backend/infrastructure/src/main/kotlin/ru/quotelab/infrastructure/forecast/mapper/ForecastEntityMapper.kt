package ru.quotelab.infrastructure.forecast.mapper

import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.ForecastStatus
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.infrastructure.common.toUtc
import ru.quotelab.infrastructure.forecast.ForecastEntity

internal fun ForecastEntity.toDomain() = Forecast(
    id = ForecastId(id),
    strategyId = StrategyId(strategyId),
    secid = Secid(secid),
    horizon = Horizon(horizonDays),
    issuedAt = issuedAt.toInstant(),
    tradeDate = tradeDate,
    basePrice = Price(basePrice),
    direction = Direction.valueOf(direction),
    confidence = Confidence(confidence),
    expectedTargetDate = expectedTargetDate,
    status = ForecastStatus.valueOf(status),
    origin = ForecastOrigin.valueOf(origin),
)

internal fun Forecast.toEntity() = ForecastEntity(
    id = id.value,
    strategyId = strategyId.value,
    secid = secid.value,
    horizonDays = horizon.days,
    issuedAt = issuedAt.toUtc(),
    tradeDate = tradeDate,
    basePrice = basePrice.value,
    direction = direction.name,
    confidence = confidence.value,
    expectedTargetDate = expectedTargetDate,
    status = status.name,
    origin = origin.name,
)
