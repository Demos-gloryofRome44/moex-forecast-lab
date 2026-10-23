package ru.quotelab.restapi.performance

import ru.quotelab.restapi.performance.dto.CalibrationBody
import ru.quotelab.restapi.performance.dto.EquityBody
import ru.quotelab.restapi.performance.dto.LeaderboardBody
import ru.quotelab.restapi.performance.dto.PerformanceBody
import ru.quotelab.restapi.strategy.dto.StrategyBody
import ru.quotelab.restapi.strategy.mapper.toBody
import ru.quotelab.usecase.performance.dto.LeaderboardQuery
import ru.quotelab.usecase.performance.dto.PerformanceQuery
import ru.quotelab.usecase.strategy.StrategyService
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
class PerformanceController(
    private val strategies: StrategyService,
    private val queries: CachedPerformanceQueries,
) {
    @HttpRoute(method = HttpMethod.GET, path = "/api/strategies")
    @Json
    fun strategies(): List<StrategyBody> = strategies.findAll().map { it.toBody() }

    @HttpRoute(method = HttpMethod.GET, path = "/api/strategies/{id}")
    @Json
    fun strategy(@Path id: UUID): StrategyBody = strategies.findById(id).toBody()

    @HttpRoute(method = HttpMethod.GET, path = "/api/strategies/{id}/performance")
    @Json
    fun performance(
        @Path id: UUID,
        @Query secid: String?,
        @Query horizon: Int?,
        @Query from: LocalDate?,
        @Query till: LocalDate?,
        @Query origin: String?,
    ): PerformanceBody = queries.performance(PerformanceQuery(id, secid?.uppercase(), horizon, from, till, origin?.uppercase()))

    @HttpRoute(method = HttpMethod.GET, path = "/api/strategies/{id}/calibration")
    @Json
    fun calibration(
        @Path id: UUID,
        @Query horizon: Int?,
        @Query origin: String?,
    ): CalibrationBody = queries.calibration(CalibrationKey(id, horizon, origin?.uppercase()))

    @HttpRoute(method = HttpMethod.GET, path = "/api/strategies/{id}/equity")
    @Json
    fun equity(
        @Path id: UUID,
        @Query secid: String?,
        @Query horizon: Int?,
        @Query origin: String?,
    ): EquityBody = queries.equity(EquityKey(id, secid?.uppercase(), horizon ?: DEFAULT_HORIZON, origin?.uppercase()))

    @HttpRoute(method = HttpMethod.GET, path = "/api/leaderboard")
    @Json
    fun leaderboard(
        @Query horizon: Int?,
        @Query from: LocalDate?,
        @Query till: LocalDate?,
        @Query origin: String?,
    ): LeaderboardBody = queries.leaderboard(LeaderboardQuery(horizon ?: DEFAULT_HORIZON, from, till, origin?.uppercase()))

    private companion object {
        const val DEFAULT_HORIZON = 1
    }
}
