package ru.quotelab.domain.forecast.access

import ru.quotelab.domain.common.Page
import ru.quotelab.domain.common.PageRequest
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.ForecastStatus
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import java.time.LocalDate

interface ForecastRepository {
    fun insertIfAbsent(forecast: Forecast): Boolean

    fun findById(id: ForecastId): Forecast?

    fun findPending(origin: ForecastOrigin, limit: Int, afterId: ForecastId?): List<Forecast>

    fun markEvaluated(id: ForecastId)

    fun search(filter: ForecastFilter, page: PageRequest): Page<Forecast>
}

data class ForecastFilter(
    val secid: Secid? = null,
    val strategyId: StrategyId? = null,
    val status: ForecastStatus? = null,
    val horizon: Horizon? = null,
    val from: LocalDate? = null,
    val till: LocalDate? = null,
    val origin: ForecastOrigin? = null,
)
