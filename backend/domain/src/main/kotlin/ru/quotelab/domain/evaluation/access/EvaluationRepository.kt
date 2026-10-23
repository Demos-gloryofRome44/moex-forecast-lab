package ru.quotelab.domain.evaluation.access

import ru.quotelab.domain.evaluation.EvaluatedForecast
import ru.quotelab.domain.evaluation.Evaluation
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import java.time.Instant
import java.time.LocalDate

interface EvaluationRepository {
    fun insertIfAbsent(evaluation: Evaluation): Boolean

    fun findByForecastIds(ids: Collection<ForecastId>): List<Evaluation>

    fun findEvaluated(filter: EvaluatedFilter): List<EvaluatedForecast>
}

data class EvaluatedFilter(
    val origin: ForecastOrigin,
    val strategyIds: Set<StrategyId>? = null,
    val secid: Secid? = null,
    val horizon: Horizon? = null,
    val from: LocalDate? = null,
    val till: LocalDate? = null,
    val evaluatedAfter: Instant? = null,
    val evaluatedUpTo: Instant? = null,
)
