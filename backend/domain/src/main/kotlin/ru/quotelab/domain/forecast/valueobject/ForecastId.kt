package ru.quotelab.domain.forecast.valueobject

import java.util.UUID

@JvmInline
value class ForecastId(val value: UUID = UUID.randomUUID()) {
    override fun toString(): String = value.toString()
}
