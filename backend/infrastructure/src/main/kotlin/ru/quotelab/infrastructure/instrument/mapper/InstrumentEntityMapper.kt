package ru.quotelab.infrastructure.instrument.mapper

import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.valueobject.Board
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.infrastructure.instrument.InstrumentEntity

internal fun InstrumentEntity.toDomain() = Instrument(
    secid = Secid(secid),
    board = Board.of(board),
    name = name,
    isActive = isActive,
)

internal fun Instrument.toEntity() = InstrumentEntity(
    secid = secid.value,
    board = board.code,
    name = name,
    isActive = isActive,
)
