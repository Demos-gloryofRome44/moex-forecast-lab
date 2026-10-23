package ru.quotelab.domain.strategy.access

import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.valueobject.StrategyId

interface StrategyRepository {
    fun findAll(): List<StrategyDescriptor>
    fun findById(id: StrategyId): StrategyDescriptor?

    fun register(descriptor: StrategyDescriptor)

    fun setActive(id: StrategyId, active: Boolean)
}
