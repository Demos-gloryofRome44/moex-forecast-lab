package ru.quotelab.infrastructure.strategy

import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.infrastructure.strategy.mapper.toDomain
import ru.quotelab.infrastructure.strategy.mapper.toEntity
import ru.tinkoff.kora.common.Component

@Component
class StrategyRepositoryImpl(
    private val dao: StrategyDao,
) : StrategyRepository {
    override fun findAll(): List<StrategyDescriptor> = dao.findAll().map { it.toDomain() }

    override fun findById(id: StrategyId): StrategyDescriptor? = dao.findById(id.value)?.toDomain()

    override fun register(descriptor: StrategyDescriptor) = dao.insert(descriptor.toEntity())

    override fun setActive(id: StrategyId, active: Boolean) = dao.setActive(id.value, active)
}
