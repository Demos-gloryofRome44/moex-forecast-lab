package ru.quotelab.usecase.strategy

import ru.quotelab.domain.strategy.Strategy
import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.domain.strategy.ensemble.MultiplicativeWeightsEnsemble
import ru.quotelab.domain.strategy.valueobject.StrategyId

class StrategyCatalog(
    val strategies: List<Strategy>,
    val ensemble: MultiplicativeWeightsEnsemble?,
) {
    init {
        val ids = descriptors().map { it.id }
        require(ids.size == ids.toSet().size) { "Duplicate strategy (code, version) in catalog" }
    }

    val ensembleMembers: List<StrategyId> get() = strategies.filter { it.descriptor.kind == StrategyKind.INDICATOR }.map { it.id }

    val maxRequiredHistory: Int get() = strategies.maxOfOrNull { it.requiredHistory } ?: 1

    fun descriptors(): List<StrategyDescriptor> = strategies.map { it.descriptor } + listOfNotNull(ensemble?.descriptor)
}
