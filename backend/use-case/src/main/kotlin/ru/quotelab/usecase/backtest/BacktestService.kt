package ru.quotelab.usecase.backtest

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.usecase.backtest.dto.BacktestReport
import ru.quotelab.usecase.backtest.dto.BacktestRequest
import ru.quotelab.usecase.common.MarketSettings
import ru.quotelab.usecase.common.QuotelabMetrics
import ru.quotelab.usecase.common.requireValidRange
import ru.quotelab.usecase.evaluation.EvaluationService
import ru.quotelab.usecase.forecast.ForecastEngine
import ru.quotelab.usecase.strategy.StrategyCatalog
import java.time.Clock
import java.time.LocalDate

class BacktestService(
    private val instruments: InstrumentRepository,
    private val candles: CandleRepository,
    private val catalog: StrategyCatalog,
    private val engine: ForecastEngine,
    private val evaluation: EvaluationService,
    private val metrics: QuotelabMetrics,
    private val marketSettings: MarketSettings,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun run(request: BacktestRequest): BacktestReport = metrics.timeJob("backtest") {
        requireValidRange(request.from, request.till)
        val till = request.till ?: LocalDate.now(clock.withZone(marketSettings.zone)).minusDays(1)
        val from = request.from ?: till.minusYears(1)
        val warmupFrom = from.minusDays(catalog.maxRequiredHistory * 2L + WARMUP_PAD_DAYS)

        val active = instruments.findActive()
        val history: Map<String, List<Candle>> = active.associate { instrument ->
            instrument.secid.value to candles.findRange(instrument.secid, CandleInterval.DAY, warmupFrom, till)
        }
        val tradeDates = history.values.flatten().map { it.tradeDate }.filter { !it.isBefore(from) }.toSortedSet()
        val candlesAfter = EvaluationService.CandlesAfter { secid, date, limit ->
            history[secid.value].orEmpty().asSequence().filter { it.tradeDate.isAfter(date) }.take(limit).toList()
        }

        log.info("Backtest {}..{}: {} trade dates, {} instruments", from, till, tradeDates.size, active.size)
        var created = 0
        var evaluated = 0
        for (tradeDate in tradeDates) {
            evaluated += evaluateUpTo(tradeDate, candlesAfter)
            val issuedAt = marketSettings.endOfDay(tradeDate)
            for (instrument in active) {
                val visible = history[instrument.secid.value].orEmpty().filter { !it.tradeDate.isAfter(tradeDate) }
                created += engine.issue(instrument, tradeDate, visible, ForecastOrigin.BACKTEST, issuedAt).created.size
            }
        }
        tradeDates.lastOrNull()?.let { evaluated += evaluateUpTo(it, candlesAfter) }
        log.info("Backtest finished: {} forecasts created, {} evaluated", created, evaluated)
        BacktestReport(from, till, tradeDates.size, created, evaluated)
    }

    private fun evaluateUpTo(tradeDate: LocalDate, candlesAfter: EvaluationService.CandlesAfter): Int =
        evaluation.evaluate(
            origin = ForecastOrigin.BACKTEST,
            lastFinalDate = tradeDate,
            candlesAfter = candlesAfter,
            evaluatedAt = { target -> marketSettings.endOfDay(target.tradeDate) },
            publish = false,
        ).evaluated

    private companion object {
        const val WARMUP_PAD_DAYS = 30L
    }
}
