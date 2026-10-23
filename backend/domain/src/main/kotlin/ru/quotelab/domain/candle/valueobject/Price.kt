package ru.quotelab.domain.candle.valueobject

import java.math.BigDecimal
import java.math.RoundingMode

@JvmInline
value class Price(val value: BigDecimal) : Comparable<Price> {
    init {
        require(value.signum() > 0) { "Price must be positive, was $value" }
    }

    override fun compareTo(other: Price): Int = value.compareTo(other.value)

    fun returnFrom(base: Price): BigDecimal =
        (value - base.value).divide(base.value, RETURN_SCALE, RoundingMode.HALF_EVEN)

    override fun toString(): String = value.toPlainString()

    companion object {
        const val RETURN_SCALE = 10

        fun of(raw: String): Price = Price(BigDecimal(raw))
    }
}
