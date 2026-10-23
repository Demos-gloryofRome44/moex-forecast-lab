package ru.quotelab.infrastructure.ensemble.mapper

import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.strategy.ensemble.EnsembleWeight
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.infrastructure.common.toUtc
import ru.quotelab.infrastructure.ensemble.EnsembleWeightEntity

internal fun EnsembleWeightEntity.toDomain() = EnsembleWeight(
    ensembleId = StrategyId(ensembleId),
    memberId = StrategyId(memberStrategyId),
    horizon = Horizon(horizonDays),
    origin = ForecastOrigin.valueOf(origin),
    effectiveAt = effectiveAt.toInstant(),
    weight = weight,
)

internal fun EnsembleWeight.toEntity() = EnsembleWeightEntity(
    ensembleId = ensembleId.value,
    memberStrategyId = memberId.value,
    horizonDays = horizon.days,
    origin = origin.name,
    effectiveAt = effectiveAt.toUtc(),
    weight = weight,
)
