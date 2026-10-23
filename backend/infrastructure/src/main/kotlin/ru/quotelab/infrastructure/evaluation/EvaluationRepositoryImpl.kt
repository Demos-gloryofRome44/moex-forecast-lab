package ru.quotelab.infrastructure.evaluation

import ru.quotelab.domain.evaluation.EvaluatedForecast
import ru.quotelab.domain.evaluation.Evaluation
import ru.quotelab.domain.evaluation.access.EvaluatedFilter
import ru.quotelab.domain.evaluation.access.EvaluationRepository
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.infrastructure.common.toIdList
import ru.quotelab.infrastructure.common.toUtc
import ru.quotelab.infrastructure.evaluation.mapper.toDomain
import ru.quotelab.infrastructure.evaluation.mapper.toEntity
import ru.tinkoff.kora.common.Component

@Component
class EvaluationRepositoryImpl(
    private val dao: EvaluationDao,
) : EvaluationRepository {
    override fun insertIfAbsent(evaluation: Evaluation): Boolean = dao.insertIfAbsent(evaluation.toEntity()).value > 0

    override fun findByForecastIds(ids: Collection<ForecastId>): List<Evaluation> =
        if (ids.isEmpty()) emptyList() else dao.findByForecastIds(ids.map { it.value }.toIdList()).map { it.toDomain() }

    override fun findEvaluated(filter: EvaluatedFilter): List<EvaluatedForecast> = with(filter) {
        if (strategyIds?.isEmpty() == true) return emptyList()
        dao.findEvaluated(
            origin = origin.name,
            strategyIds = strategyIds?.map { it.value }?.toIdList(),
            secid = secid?.value,
            horizon = horizon?.days,
            from = from,
            till = till,
            evaluatedAfter = evaluatedAfter?.toUtc(),
            evaluatedUpTo = evaluatedUpTo?.toUtc(),
        ).map { it.toDomain() }
    }
}
