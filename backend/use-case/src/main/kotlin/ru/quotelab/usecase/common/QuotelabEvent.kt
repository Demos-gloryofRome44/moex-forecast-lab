package ru.quotelab.usecase.common

import ru.quotelab.usecase.forecast.dto.ForecastResponse
import java.time.LocalDate

sealed interface QuotelabEvent {
    val type: String

    data class ForecastCreated(val forecast: ForecastResponse) : QuotelabEvent {
        override val type = "forecast.created"
    }

    data class ForecastEvaluated(val forecast: ForecastResponse) : QuotelabEvent {
        override val type = "forecast.evaluated"
    }

    data class CandleSynced(val secid: String, val candles: Int, val lastTradeDate: LocalDate?) : QuotelabEvent {
        override val type = "candle.synced"
    }
}

fun interface EventPublisher {
    fun publish(event: QuotelabEvent)

    companion object {
        val NOOP = EventPublisher { }
    }
}
