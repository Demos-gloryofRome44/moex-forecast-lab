package ru.quotelab.usecase.strategy

import org.slf4j.LoggerFactory
import ru.quotelab.domain.problems.StrategyConfigurationException
import ru.quotelab.domain.problems.StrategyNotFoundException
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.usecase.strategy.dto.StrategyResponse
import ru.quotelab.usecase.strategy.mapper.toResponse
import java.util.UUID

class StrategyService(
    private val repository: StrategyRepository,
    private val catalog: StrategyCatalog,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findAll(): List<StrategyResponse> =
        repository.findAll().sortedWith(compareBy({ it.kind }, { it.code }, { it.version })).map { it.toResponse() }

    fun findById(id: UUID): StrategyResponse =
        (repository.findById(StrategyId(id)) ?: throw StrategyNotFoundException(id.toString())).toResponse()

    fun registerCatalog() {
        val current = catalog.descriptors()
        current.forEach { descriptor ->
            val existing = repository.findById(descriptor.id)
            if (existing != null && existing.params != descriptor.params) {
                throw StrategyConfigurationException(
                    "Strategy ${descriptor.code} v${descriptor.version} is already registered with params ${existing.params}, " +
                        "but configuration has ${descriptor.params}. Bump the version to change parameters.",
                )
            }
            if (existing == null) {
                log.info("Registering strategy {} v{} {}", descriptor.code, descriptor.version, descriptor.params)
                repository.register(descriptor)
            } else if (!existing.isActive) {
                repository.setActive(descriptor.id, true)
            }
        }
        val currentIds = current.map { it.id }.toSet()
        repository.findAll().filter { it.isActive && it.id !in currentIds }.forEach {
            log.info("Deactivating strategy {} v{} (not in catalog)", it.code, it.version)
            repository.setActive(it.id, false)
        }
    }
}
