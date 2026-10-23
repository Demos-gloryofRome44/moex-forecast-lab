package ru.quotelab.domain.strategy

import ru.quotelab.domain.strategy.valueobject.Confidence

data class Signal(
    val direction: Direction,
    val confidence: Confidence,
) {
    companion object {
        val FLAT = Signal(Direction.FLAT, Confidence.NEUTRAL)
    }
}
