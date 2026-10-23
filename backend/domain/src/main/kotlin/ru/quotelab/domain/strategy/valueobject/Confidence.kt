package ru.quotelab.domain.strategy.valueobject

@JvmInline
value class Confidence(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Confidence must be within 0..1, was $value" }
    }

    companion object {
        val NEUTRAL = Confidence(0.5)

        fun clamped(value: Double): Confidence = Confidence(value.coerceIn(0.0, 1.0))
    }
}
