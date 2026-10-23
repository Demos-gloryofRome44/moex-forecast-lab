package ru.quotelab.restapi.stream

import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDate

@Json
data class CandleSyncedBody(
    val secid: String,
    val candles: Int,
    val lastTradeDate: LocalDate?,
)
