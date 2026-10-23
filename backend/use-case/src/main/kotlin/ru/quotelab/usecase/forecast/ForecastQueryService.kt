package ru.quotelab.usecase.forecast

import ru.quotelab.domain.common.PageRequest
import ru.quotelab.domain.evaluation.access.EvaluationRepository
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.access.ForecastFilter
import ru.quotelab.domain.forecast.access.ForecastRepository
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.problems.ForecastNotFoundException
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.usecase.common.parseEnum
import ru.quotelab.usecase.common.parseStatus
import ru.quotelab.usecase.common.requireValidRange
import ru.quotelab.usecase.forecast.dto.ForecastPageResponse
import ru.quotelab.usecase.forecast.dto.ForecastResponse
import ru.quotelab.usecase.forecast.dto.ForecastSearchRequest
import ru.quotelab.usecase.forecast.mapper.toResponse
import java.util.UUID

class ForecastQueryService(
    private val forecasts: ForecastRepository,
    private val evaluations: EvaluationRepository,
    private val strategies: StrategyRepository,
) {
    fun search(request: ForecastSearchRequest): ForecastPageResponse {
        requireValidRange(request.from, request.till)
        val filter = ForecastFilter(
            secid = request.secid?.let(Secid::of),
            strategyId = request.strategyId?.let(::StrategyId),
            status = parseStatus(request.status),
            horizon = request.horizon?.let(::Horizon),
            from = request.from,
            till = request.till,
            origin = request.origin?.let { parseEnum<ForecastOrigin>(it, "origin") },
        )
        val page = forecasts.search(filter, PageRequest(request.page, request.size))
        val evaluationsById = evaluations.findByForecastIds(page.items.map { it.id }).associateBy { it.forecastId }
        val descriptors = strategies.findAll().associateBy { it.id }
        return ForecastPageResponse(
            items = page.items.map { it.toResponse(descriptors[it.strategyId], evaluationsById[it.id]) },
            page = page.page,
            size = page.size,
            total = page.total,
        )
    }

    fun findById(id: UUID): ForecastResponse {
        val forecast = forecasts.findById(ForecastId(id)) ?: throw ForecastNotFoundException(id.toString())
        val evaluation = evaluations.findByForecastIds(listOf(forecast.id)).firstOrNull()
        return forecast.toResponse(strategies.findById(forecast.strategyId), evaluation)
    }
}
