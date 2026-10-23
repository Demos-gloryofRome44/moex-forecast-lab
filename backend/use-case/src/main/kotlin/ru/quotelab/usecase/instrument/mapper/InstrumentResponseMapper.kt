package ru.quotelab.usecase.instrument.mapper

import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.usecase.instrument.dto.InstrumentResponse
import java.time.LocalDate

internal fun Instrument.toResponse(lastTradeDate: LocalDate?) = InstrumentResponse(
    secid = secid.value,
    board = board.code,
    name = name,
    isActive = isActive,
    lastTradeDate = lastTradeDate,
)
