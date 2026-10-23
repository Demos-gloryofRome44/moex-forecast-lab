package ru.quotelab.usecase.candle.dto

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class CandleQuery(
    val secid: String,
    val from: LocalDate?,
    val till: LocalDate?,
    val interval: Int?,
)

data class CandleResponse(
    val tradeDate: LocalDate,
    val beginAt: Instant,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long,
)

data class SyncReport(
    val instruments: List<InstrumentSyncResult>,
) {
    val ok: Boolean get() = instruments.all { it.error == null }
}

data class InstrumentSyncResult(
    val secid: String,
    val candles: Int,
    val lastTradeDate: LocalDate?,
    val error: String?,
)
