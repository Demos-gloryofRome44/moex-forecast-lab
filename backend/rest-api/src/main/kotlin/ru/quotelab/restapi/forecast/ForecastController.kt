package ru.quotelab.restapi.forecast

import ru.quotelab.domain.problems.InvalidRequestException
import ru.quotelab.restapi.common.ApiConfig
import ru.quotelab.restapi.forecast.dto.ForecastBody
import ru.quotelab.restapi.forecast.dto.ForecastPageBody
import ru.quotelab.restapi.forecast.mapper.toBody
import ru.quotelab.usecase.forecast.ForecastQueryService
import ru.quotelab.usecase.forecast.dto.ForecastSearchRequest
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDate
import java.util.UUID

@Component
@HttpController
class ForecastController(
    private val forecasts: ForecastQueryService,
    private val config: ApiConfig,
) {
    @HttpRoute(method = HttpMethod.GET, path = "/api/forecasts")
    @Json
    fun search(
        @Query secid: String?,
        @Query strategyId: UUID?,
        @Query status: String?,
        @Query horizon: Int?,
        @Query origin: String?,
        @Query from: LocalDate?,
        @Query till: LocalDate?,
        @Query page: Int?,
        @Query size: Int?,
    ): ForecastPageBody {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        if (pageSize !in 1..config.maxPageSize()) {
            throw InvalidRequestException("Invalid page size", listOf("size must be within 1..${config.maxPageSize()}"))
        }
        if ((page ?: 0) < 0) throw InvalidRequestException("Invalid page", listOf("page must be >= 0"))
        return forecasts.search(
            ForecastSearchRequest(secid, strategyId, status, horizon, origin, from, till, page ?: 0, pageSize),
        ).toBody()
    }

    @HttpRoute(method = HttpMethod.GET, path = "/api/forecasts/{id}")
    @Json
    fun get(@Path id: UUID): ForecastBody = forecasts.findById(id).toBody()

    private companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}
