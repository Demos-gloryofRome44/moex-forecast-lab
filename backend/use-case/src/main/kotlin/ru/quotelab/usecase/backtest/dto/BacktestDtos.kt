package ru.quotelab.usecase.backtest.dto

import java.time.LocalDate

data class BacktestRequest(
    val from: LocalDate?,
    val till: LocalDate?,
)

data class BacktestReport(
    val from: LocalDate,
    val till: LocalDate,
    val tradeDates: Int,
    val created: Int,
    val evaluated: Int,
)
