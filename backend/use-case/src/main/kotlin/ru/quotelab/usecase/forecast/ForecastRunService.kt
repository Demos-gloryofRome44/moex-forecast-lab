package ru.quotelab.usecase.forecast

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.usecase.common.EventPublisher
import ru.quotelab.usecase.common.ForecastSettings
import ru.quotelab.usecase.common.MarketSettings
import ru.quotelab.usecase.common.QuotelabEvent
import ru.quotelab.usecase.common.QuotelabMetrics
import ru.quotelab.usecase.forecast.dto.ForecastRunReport
import ru.quotelab.usecase.forecast.mapper.toResponse
import ru.quotelab.usecase.strategy.StrategyCatalog
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ForecastRunService(
    private val instruments: InstrumentRepository,
    private val candles: CandleRepository,
    private val strategies: StrategyRepository,
    private val catalog: StrategyCatalog,
    private val engine: ForecastEngine,
    private val events: EventPublisher,
    private val metrics: QuotelabMetrics,
    private val marketSettings: MarketSettings,
    private val settings: ForecastSettings,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun runLive(): ForecastRunReport = metrics.timeJob("forecast-run") {
        val now = clock.instant()
        val today = LocalDate.now(clock.withZone(marketSettings.zone))
        val descriptors = strategies.findAll().associateBy { it.id }
        val tradeDates = mutableMapOf<String, LocalDate>()
        var created = 0
        var insufficient = 0

        val fresh = instruments.findActive().mapNotNull { instrument ->
            val tradeDate = candles.findLastTradeDate(instrument.secid, CandleInterval.DAY)
            when {
                tradeDate == null -> null
                ChronoUnit.DAYS.between(tradeDate, today) > settings.maxStaleDays -> {
                    log.warn("Last candle of {} is {} — data is stale, no live forecasts issued", instrument.secid, tradeDate)
                    null
                }
                else -> instrument to tradeDate
            }
        }
        for ((instrument, tradeDate) in fresh) {
            val history = candles.findHistory(instrument.secid, CandleInterval.DAY, tradeDate, catalog.maxRequiredHistory)
            val result = engine.issue(instrument, tradeDate, history, ForecastOrigin.LIVE, now)
            result.created.forEach { events.publish(QuotelabEvent.ForecastCreated(it.toResponse(descriptors[it.strategyId], null))) }
            tradeDates[instrument.secid.value] = tradeDate
            created += result.created.size
            insufficient += result.insufficientData
        }
        metrics.forecastsCreated(created)
        log.info("Live forecast run: {} created, {} skipped for insufficient history", created, insufficient)
        ForecastRunReport(tradeDates, created, insufficient)
    }
}
