package ru.quotelab.domain.strategy.access

import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.strategy.ensemble.EnsembleWeight
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import java.time.Instant

interface EnsembleWeightRepository {
    fun append(weights: List<EnsembleWeight>)

    fun findEffective(ensembleId: StrategyId, horizon: Horizon, origin: ForecastOrigin, asOf: Instant): List<EnsembleWeight>

    fun findHistory(ensembleId: StrategyId, horizon: Horizon, origin: ForecastOrigin): List<EnsembleWeight>
}
