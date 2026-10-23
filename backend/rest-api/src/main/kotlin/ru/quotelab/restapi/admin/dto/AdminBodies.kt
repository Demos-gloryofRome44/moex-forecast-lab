package ru.quotelab.restapi.admin.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDate

@Json
data class SyncReportBody(
    val ok: Boolean,
    val instruments: List<InstrumentSyncBody>,
)

@Json
data class InstrumentSyncBody(
    val secid: String,
    val candles: Int,
    val lastTradeDate: LocalDate?,
    val error: String?,
)

@Json
data class ForecastRunBody(
    val tradeDates: Map<String, LocalDate>,
    val created: Int,
    val skippedInsufficientData: Int,
)

@Json
data class EvaluationRunBody(
    val checked: Int,
    val evaluated: Int,
    val waitingForTarget: Int,
)

@Json
data class BacktestStartedBody(
    val status: String,
    val from: LocalDate?,
    val till: LocalDate?,
)
