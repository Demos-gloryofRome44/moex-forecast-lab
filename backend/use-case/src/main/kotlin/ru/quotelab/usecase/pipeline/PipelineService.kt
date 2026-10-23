package ru.quotelab.usecase.pipeline

import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.usecase.backtest.BacktestService
import ru.quotelab.usecase.backtest.dto.BacktestReport
import ru.quotelab.usecase.backtest.dto.BacktestRequest
import ru.quotelab.usecase.candle.CandleSyncService
import ru.quotelab.usecase.candle.dto.SyncReport
import ru.quotelab.usecase.common.JobLock
import ru.quotelab.usecase.ensemble.EnsembleWeightsService
import ru.quotelab.usecase.evaluation.EvaluationService
import ru.quotelab.usecase.evaluation.dto.EvaluationRunReport
import ru.quotelab.usecase.forecast.ForecastRunService
import ru.quotelab.usecase.forecast.dto.ForecastRunReport
import java.time.Clock

class PipelineService(
    private val lock: JobLock,
    private val sync: CandleSyncService,
    private val forecastRun: ForecastRunService,
    private val evaluation: EvaluationService,
    private val ensembleWeights: EnsembleWeightsService,
    private val backtest: BacktestService,
    private val clock: Clock,
) {
    val isBusy: Boolean get() = lock.isBusy

    fun syncCandles(): SyncReport = lock.exclusive { sync.syncAll() }

    fun runForecasts(): ForecastRunReport = lock.exclusive {
        evaluation.evaluateLive()
        ensembleWeights.updateAll(ForecastOrigin.LIVE, clock.instant())
        forecastRun.runLive()
    }

    fun evaluate(): EvaluationRunReport = lock.exclusive { evaluation.evaluateLive() }

    fun updateEnsembleWeights() = lock.exclusive { ensembleWeights.updateAll(ForecastOrigin.LIVE, clock.instant()) }

    fun backtest(request: BacktestRequest): BacktestReport = lock.exclusive { backtest.run(request) }
}
