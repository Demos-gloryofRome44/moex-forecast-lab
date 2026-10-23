package ru.quotelab.domain.strategy.valueobject

@JvmInline
value class Horizon(val days: Int) {
    init {
        require(days in 1..MAX_DAYS) { "Horizon must be within 1..$MAX_DAYS trading days, was $days" }
    }

    override fun toString(): String = "${days}d"

    companion object {
        const val MAX_DAYS = 250
    }
}
