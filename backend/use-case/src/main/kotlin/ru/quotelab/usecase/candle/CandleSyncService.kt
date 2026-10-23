package ru.quotelab.usecase.candle

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.forecast.TradingCalendar
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.access.MarketDataProvider
import ru.quotelab.usecase.candle.dto.InstrumentSyncResult
import ru.quotelab.usecase.candle.dto.SyncReport
import ru.quotelab.usecase.common.EventPublisher
import ru.quotelab.usecase.common.MarketSettings
import ru.quotelab.usecase.common.QuotelabEvent
import ru.quotelab.usecase.common.QuotelabMetrics
import java.time.Clock
import java.time.LocalDate

class CandleSyncService(
    private val instruments: InstrumentRepository,
    private val candles: CandleRepository,
    private val marketData: MarketDataProvider,
    private val events: EventPublisher,
    private val metrics: QuotelabMetrics,
    private val settings: MarketSettings,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun syncAll(): SyncReport = metrics.timeJob("candle-sync") {
        SyncReport(instruments.findActive().map(::sync))
    }

    private fun sync(instrument: Instrument): InstrumentSyncResult {
        val interval = CandleInterval.DAY
        val today = LocalDate.now(clock.withZone(settings.zone))
        val last = candles.findLastTradeDate(instrument.secid, interval)
        val from = last ?: today.minusYears(settings.initialLoadYears)
        return try {
            val fetched = marketData.fetchCandles(instrument, interval, from, today)
                .filter { settings.includeWeekendSessions || TradingCalendar.isWeekday(it.tradeDate) }
            val written = if (fetched.isEmpty()) 0 else candles.upsertAll(fetched)
            val lastTradeDate = fetched.maxOfOrNull { it.tradeDate } ?: last
            log.info("Synced {} candles for {} from {} (last trade date {})", written, instrument.secid, from, lastTradeDate)
            metrics.candlesSynced(instrument.secid.value, written)
            events.publish(QuotelabEvent.CandleSynced(instrument.secid.value, written, lastTradeDate))
            InstrumentSyncResult(instrument.secid.value, written, lastTradeDate, null)
        } catch (e: RuntimeException) {
            log.warn("Candle sync failed for {}: {}", instrument.secid, e.message)
            metrics.syncFailed(instrument.secid.value)
            InstrumentSyncResult(instrument.secid.value, 0, last, e.message ?: e.javaClass.simpleName)
        }
    }
}
