package ru.quotelab.usecase.ensemble

import ru.quotelab.domain.evaluation.access.EvaluatedFilter
import ru.quotelab.domain.evaluation.access.EvaluationRepository
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.strategy.access.EnsembleWeightRepository
import ru.quotelab.domain.strategy.ensemble.EnsembleWeight
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.usecase.common.EvaluationSettings
import ru.quotelab.usecase.common.ForecastSettings
import ru.quotelab.usecase.strategy.StrategyCatalog
import java.math.BigDecimal
import java.math.MathContext
import java.time.Instant

class EnsembleWeightsService(
    private val catalog: StrategyCatalog,
    private val weights: EnsembleWeightRepository,
    private val evaluations: EvaluationRepository,
    private val settings: ForecastSettings,
    private val evaluationSettings: EvaluationSettings,
) {
    fun updateAll(origin: ForecastOrigin, at: Instant) {
        settings.horizons.forEach { weightsAsOf(origin, it, at) }
    }

    fun weightsAsOf(origin: ForecastOrigin, horizon: Horizon, at: Instant): Map<StrategyId, Double> {
        val ensemble = catalog.ensemble ?: return emptyMap()
        val members = catalog.ensembleMembers
        if (members.isEmpty()) return emptyMap()

        val snapshot = weights.findEffective(ensemble.id, horizon, origin, at)
        val sameMembers = snapshot.map { it.memberId }.toSet() == members.toSet()
        val base = if (sameMembers) snapshot.associate { it.memberId to it.weight.toDouble() } else ensemble.initialWeights(members)
        val since = if (sameMembers) snapshot.first().effectiveAt else null

        val fresh = evaluations.findEvaluated(
            EvaluatedFilter(
                origin = origin,
                strategyIds = members.toSet(),
                horizon = horizon,
                evaluatedAfter = since,
                evaluatedUpTo = at,
            ),
        )
        if (fresh.isEmpty()) {
            if (!sameMembers) weights.append(snapshotRows(ensemble.id, horizon, origin, at, base))
            return base
        }
        val updated = fresh.fold(base) { acc, item ->
            val marketMoved = item.evaluation.actualReturn.abs() >= evaluationSettings.flatThreshold
            val loss = ensemble.loss(item.forecast.direction, item.evaluation.outcome, marketMoved)
            if (loss == null) acc else ensemble.update(acc, mapOf(item.forecast.strategyId to loss))
        }
        weights.append(snapshotRows(ensemble.id, horizon, origin, fresh.maxOf { it.evaluation.evaluatedAt }, updated))
        return updated
    }

    fun history(origin: ForecastOrigin, horizon: Horizon): List<EnsembleWeight> {
        val ensemble = catalog.ensemble ?: return emptyList()
        return weights.findHistory(ensemble.id, horizon, origin)
    }

    private fun snapshotRows(
        ensembleId: StrategyId,
        horizon: Horizon,
        origin: ForecastOrigin,
        at: Instant,
        values: Map<StrategyId, Double>,
    ) = values.map { (member, weight) ->
        EnsembleWeight(ensembleId, member, horizon, origin, at, BigDecimal(weight, MathContext.DECIMAL64))
    }
}
