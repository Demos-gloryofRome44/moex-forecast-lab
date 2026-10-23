package ru.quotelab.usecase.evaluation

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.evaluation.Evaluation
import ru.quotelab.domain.evaluation.ForecastEvaluator
import ru.quotelab.domain.evaluation.access.EvaluationRepository
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.ForecastStatus
import ru.quotelab.domain.forecast.access.ForecastRepository
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.usecase.common.EvaluationSettings
import ru.quotelab.usecase.common.EventPublisher
import ru.quotelab.usecase.common.MarketSettings
import ru.quotelab.usecase.common.QuotelabEvent
import ru.quotelab.usecase.common.QuotelabMetrics
import ru.quotelab.usecase.common.TransactionRunner
import ru.quotelab.usecase.evaluation.dto.EvaluationRunReport
import ru.quotelab.usecase.forecast.mapper.toResponse
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class EvaluationService(
    private val forecasts: ForecastRepository,
    private val evaluations: EvaluationRepository,
    private val candles: CandleRepository,
    private val strategies: StrategyRepository,
    private val tx: TransactionRunner,
    private val events: EventPublisher,
    private val metrics: QuotelabMetrics,
    private val marketSettings: MarketSettings,
    private val settings: EvaluationSettings,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun interface CandlesAfter {
        fun find(secid: Secid, tradeDate: LocalDate, limit: Int): List<Candle>
    }

    fun evaluateLive(): EvaluationRunReport = metrics.timeJob("evaluation") {
        val lastFinalDate = LocalDate.now(clock.withZone(marketSettings.zone)).minusDays(1)
        val report = evaluate(
            origin = ForecastOrigin.LIVE,
            lastFinalDate = lastFinalDate,
            candlesAfter = { secid, date, limit -> candles.findAfter(secid, CandleInterval.DAY, date, limit) },
            evaluatedAt = { clock.instant() },
            publish = true,
        )
        metrics.forecastsEvaluated(report.evaluated)
        report
    }

    fun evaluate(
        origin: ForecastOrigin,
        lastFinalDate: LocalDate,
        candlesAfter: CandlesAfter,
        evaluatedAt: (targetCandle: Candle) -> Instant,
        publish: Boolean,
    ): EvaluationRunReport {
        var checked = 0
        var evaluated = 0
        var cursor: ForecastId? = null
        val descriptors by lazy { strategies.findAll().associateBy { it.id } }
        val onEvaluated: (Forecast, Evaluation) -> Unit = { forecast, evaluation ->
            if (publish) {
                val response = forecast.copy(status = ForecastStatus.EVALUATED).toResponse(descriptors[forecast.strategyId], evaluation)
                events.publish(QuotelabEvent.ForecastEvaluated(response))
            }
        }

        do {
            val batch = forecasts.findPending(origin, settings.batchSize, cursor)
            cursor = batch.lastOrNull()?.id
            checked += batch.size
            evaluated += batch.groupBy { it.secid to it.tradeDate }.entries.sumOf { (key, group) ->
                evaluateGroup(key.first, key.second, group, lastFinalDate, candlesAfter, evaluatedAt, onEvaluated)
            }
        } while (batch.size == settings.batchSize)

        val waiting = checked - evaluated
        if (evaluated > 0) log.info("Evaluated {} {} forecasts ({} waiting for target)", evaluated, origin, waiting)
        return EvaluationRunReport(checked, evaluated, waiting)
    }

    private fun evaluateGroup(
        secid: Secid,
        tradeDate: LocalDate,
        group: List<Forecast>,
        lastFinalDate: LocalDate,
        candlesAfter: CandlesAfter,
        evaluatedAt: (Candle) -> Instant,
        onEvaluated: (Forecast, Evaluation) -> Unit,
    ): Int {
        val after = candlesAfter.find(secid, tradeDate, group.maxOf { it.horizon.days })
        return group.count { forecast ->
            val target = after.getOrNull(forecast.horizon.days - 1)?.takeUnless { it.tradeDate.isAfter(lastFinalDate) }
            val evaluation = target?.let { ForecastEvaluator.evaluate(forecast, it, settings.flatThreshold, evaluatedAt(it)) }
            val closed = evaluation != null && close(forecast, evaluation)
            if (closed) onEvaluated(forecast, evaluation!!)
            closed
        }
    }

    private fun close(forecast: Forecast, evaluation: Evaluation): Boolean = tx.inTransaction {
        val inserted = evaluations.insertIfAbsent(evaluation)
        forecasts.markEvaluated(forecast.id)
        inserted
    }
}
