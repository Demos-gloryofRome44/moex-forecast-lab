package ru.quotelab.restapi.stream

import ru.quotelab.restapi.forecast.dto.ForecastBody
import ru.quotelab.restapi.forecast.mapper.toBody
import ru.quotelab.restapi.performance.CachedPerformanceQueries
import ru.quotelab.usecase.common.EventPublisher
import ru.quotelab.usecase.common.QuotelabEvent
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.json.common.JsonWriter

@Component
class RestApiEventPublisher(
    private val broadcaster: SseBroadcaster,
    private val caches: CachedPerformanceQueries,
    private val forecastWriter: JsonWriter<ForecastBody>,
    private val candleWriter: JsonWriter<CandleSyncedBody>,
) : EventPublisher {
    override fun publish(event: QuotelabEvent) {
        val json = when (event) {
            is QuotelabEvent.ForecastCreated -> forecastWriter.toStringUnchecked(event.forecast.toBody())
            is QuotelabEvent.ForecastEvaluated -> {
                caches.invalidateAll()
                forecastWriter.toStringUnchecked(event.forecast.toBody())
            }
            is QuotelabEvent.CandleSynced ->
                candleWriter.toStringUnchecked(CandleSyncedBody(event.secid, event.candles, event.lastTradeDate))
        }
        if (broadcaster.subscribers > 0) broadcaster.broadcast(event.type, json)
    }
}
