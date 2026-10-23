package ru.quotelab.restapi.admin.mapper

import ru.quotelab.restapi.admin.dto.EvaluationRunBody
import ru.quotelab.restapi.admin.dto.ForecastRunBody
import ru.quotelab.restapi.admin.dto.InstrumentSyncBody
import ru.quotelab.restapi.admin.dto.SyncReportBody
import ru.quotelab.usecase.candle.dto.SyncReport
import ru.quotelab.usecase.evaluation.dto.EvaluationRunReport
import ru.quotelab.usecase.forecast.dto.ForecastRunReport

internal fun SyncReport.toBody() = SyncReportBody(
    ok = ok,
    instruments = instruments.map { InstrumentSyncBody(it.secid, it.candles, it.lastTradeDate, it.error) },
)

internal fun ForecastRunReport.toBody() = ForecastRunBody(tradeDates, created, skippedInsufficientData)

internal fun EvaluationRunReport.toBody() = EvaluationRunBody(checked, evaluated, waitingForTarget)
