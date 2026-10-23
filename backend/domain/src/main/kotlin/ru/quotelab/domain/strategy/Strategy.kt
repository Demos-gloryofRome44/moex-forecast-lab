package ru.quotelab.domain.strategy

import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId

interface Strategy {
    val descriptor: StrategyDescriptor

    val id: StrategyId get() = descriptor.id

    val requiredHistory: Int

    fun signal(history: CandleSeries, horizon: Horizon): StrategyResult
}
