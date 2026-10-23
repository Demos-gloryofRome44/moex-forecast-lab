package ru.quotelab.app.jobs

import org.slf4j.LoggerFactory
import ru.quotelab.app.config.QuotelabConfig
import ru.quotelab.usecase.pipeline.PipelineService
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.scheduling.quartz.ScheduleWithCron

@Component
class CandleSyncJob(
    private val pipeline: PipelineService,
    private val forecastRunJob: ForecastRunJob,
    private val config: QuotelabConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ScheduleWithCron(value = "0 15 19 ? * MON-FRI", config = "quotelab.schedule.candleSync")
    fun run() {
        if (!config.jobs().enabled()) return
        val report = pipeline.syncCandles()
        if (report.instruments.any { it.error == null }) {
            forecastRunJob.run()
        } else {
            log.warn("Candle sync failed for every instrument, forecasts are not issued")
        }
    }
}

@Component
class ForecastRunJob(
    private val pipeline: PipelineService,
) {
    fun run() {
        pipeline.runForecasts()
    }
}

@Component
class EvaluationJob(
    private val pipeline: PipelineService,
    private val ensembleWeightsJob: EnsembleWeightsJob,
    private val config: QuotelabConfig,
) {
    @ScheduleWithCron(value = "0 5 * * * ?", config = "quotelab.schedule.evaluation")
    fun run() {
        if (!config.jobs().enabled()) return
        val report = pipeline.evaluate()
        if (report.evaluated > 0) ensembleWeightsJob.run()
    }
}

@Component
class EnsembleWeightsJob(
    private val pipeline: PipelineService,
) {
    fun run() {
        pipeline.updateEnsembleWeights()
    }
}
