package ru.quotelab.infrastructure.ensemble

import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.strategy.access.EnsembleWeightRepository
import ru.quotelab.domain.strategy.ensemble.EnsembleWeight
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.infrastructure.common.toUtc
import ru.quotelab.infrastructure.ensemble.mapper.toDomain
import ru.quotelab.infrastructure.ensemble.mapper.toEntity
import ru.tinkoff.kora.common.Component
import java.time.Instant

@Component
class EnsembleWeightRepositoryImpl(
    private val dao: EnsembleWeightDao,
) : EnsembleWeightRepository {
    override fun append(weights: List<EnsembleWeight>) {
        if (weights.isNotEmpty()) dao.append(weights.map { it.toEntity() })
    }

    override fun findEffective(ensembleId: StrategyId, horizon: Horizon, origin: ForecastOrigin, asOf: Instant): List<EnsembleWeight> =
        dao.findEffective(ensembleId.value, horizon.days, origin.name, asOf.toUtc()).map { it.toDomain() }

    override fun findHistory(ensembleId: StrategyId, horizon: Horizon, origin: ForecastOrigin): List<EnsembleWeight> =
        dao.findHistory(ensembleId.value, horizon.days, origin.name).map { it.toDomain() }
}
