package ru.quotelab.restapi.admin

import org.slf4j.LoggerFactory
import ru.quotelab.domain.problems.NotFoundException
import ru.quotelab.restapi.admin.dto.BacktestStartedBody
import ru.quotelab.restapi.admin.dto.EvaluationRunBody
import ru.quotelab.restapi.admin.dto.ForecastRunBody
import ru.quotelab.restapi.admin.dto.SyncReportBody
import ru.quotelab.restapi.admin.mapper.toBody
import ru.quotelab.restapi.common.ApiConfig
import ru.quotelab.restapi.common.BusyException
import ru.quotelab.restapi.performance.CachedPerformanceQueries
import ru.quotelab.usecase.backtest.dto.BacktestRequest
import ru.quotelab.usecase.pipeline.PipelineService
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDate
import kotlin.concurrent.thread

@Component
@HttpController
class AdminController(
    private val pipeline: PipelineService,
    private val caches: CachedPerformanceQueries,
    private val config: ApiConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @HttpRoute(method = HttpMethod.POST, path = "/api/admin/sync")
    @Json
    fun sync(): SyncReportBody = guarded { pipeline.syncCandles().toBody() }

    @HttpRoute(method = HttpMethod.POST, path = "/api/admin/run-forecasts")
    @Json
    fun runForecasts(): ForecastRunBody = guarded { pipeline.runForecasts().toBody() }

    @HttpRoute(method = HttpMethod.POST, path = "/api/admin/evaluate")
    @Json
    fun evaluate(): EvaluationRunBody = guarded {
        val report = pipeline.evaluate()
        pipeline.updateEnsembleWeights()
        report.toBody()
    }

    @HttpRoute(method = HttpMethod.POST, path = "/api/admin/backtest")
    @Json
    fun backtest(@Query from: LocalDate?, @Query till: LocalDate?): HttpResponseEntity<BacktestStartedBody> = guarded {
        thread(name = "backtest", isDaemon = true) {
            try {
                val report = pipeline.backtest(BacktestRequest(from, till))
                log.info("Backtest done: {}", report)
            } catch (e: Exception) {
                log.error("Backtest failed", e)
            } finally {
                caches.invalidateAll()
            }
        }
        HttpResponseEntity.of(ACCEPTED, BacktestStartedBody("started", from, till))
    }

    private fun <T> guarded(block: () -> T): T {
        if (!config.adminEnabled()) throw AdminDisabledException()
        if (pipeline.isBusy) throw BusyException("Another job is running, try again later")
        return block()
    }

    private class AdminDisabledException : NotFoundException("Admin endpoints are available only in the dev profile")

    private companion object {
        const val ACCEPTED = 202
    }
}
