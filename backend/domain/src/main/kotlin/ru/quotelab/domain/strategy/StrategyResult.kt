package ru.quotelab.domain.strategy

sealed interface StrategyResult {
    data class Success(val signal: Signal) : StrategyResult

    data class InsufficientData(val required: Int, val available: Int) : StrategyResult
}
