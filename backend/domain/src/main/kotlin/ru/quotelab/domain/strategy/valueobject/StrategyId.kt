package ru.quotelab.domain.strategy.valueobject

import java.util.UUID

@JvmInline
value class StrategyId(val value: UUID = UUID.randomUUID()) {
    override fun toString(): String = value.toString()

    companion object {
        fun of(code: String, version: String): StrategyId =
            StrategyId(UUID.nameUUIDFromBytes("strategy:$code:$version".toByteArray()))
    }
}
