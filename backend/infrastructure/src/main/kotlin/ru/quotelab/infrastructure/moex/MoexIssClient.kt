package ru.quotelab.infrastructure.moex

import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout

@HttpClient(configPath = "httpClient.moex")
interface MoexIssClient {
    @CircuitBreaker("moex")
    @Retry("moex")
    @Timeout("moex")
    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/iss/engines/{engine}/markets/{market}/boards/{board}/securities/{secid}/candles.json")
    fun candles(
        @Path engine: String,
        @Path market: String,
        @Path board: String,
        @Path secid: String,
        @Query("from") from: String,
        @Query("till") till: String,
        @Query("interval") interval: Int,
        @Query("start") start: Int,
        @Query("iss.meta") meta: String = "off",
    ): IssResponse

    @CircuitBreaker("moex")
    @Retry("moex")
    @Timeout("moex")
    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/iss/securities/{secid}.json")
    fun security(
        @Path secid: String,
        @Query("iss.meta") meta: String = "off",
        @Query("iss.only") only: String = "description,boards",
    ): IssResponse
}
