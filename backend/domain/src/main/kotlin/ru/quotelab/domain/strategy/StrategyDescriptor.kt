package ru.quotelab.domain.strategy

import ru.quotelab.domain.strategy.valueobject.StrategyId

data class StrategyDescriptor(
    val code: String,
    val version: String,
    val kind: StrategyKind,
    val params: Map<String, String>,
    val isActive: Boolean = true,
) {
    val id: StrategyId get() = StrategyId.of(code, version)

    init {
        require(code.isNotBlank()) { "Strategy code must not be blank" }
        require(version.isNotBlank()) { "Strategy version must not be blank" }
    }
}
