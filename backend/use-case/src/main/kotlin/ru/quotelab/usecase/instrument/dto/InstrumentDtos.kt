package ru.quotelab.usecase.instrument.dto

import java.time.LocalDate

data class AddInstrumentRequest(
    val secid: String,
    val board: String?,
)

data class InstrumentResponse(
    val secid: String,
    val board: String,
    val name: String,
    val isActive: Boolean,
    val lastTradeDate: LocalDate?,
)
