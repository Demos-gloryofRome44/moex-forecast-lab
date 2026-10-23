package ru.quotelab.domain.instrument

import ru.quotelab.domain.instrument.valueobject.Board
import ru.quotelab.domain.instrument.valueobject.Secid

data class Instrument(
    val secid: Secid,
    val board: Board,
    val name: String,
    val isActive: Boolean = true,
)
