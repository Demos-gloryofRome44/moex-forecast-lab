package ru.quotelab.usecase.strategy.mapper

import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.usecase.strategy.dto.StrategyResponse

internal fun StrategyDescriptor.toResponse() = StrategyResponse(
    id = id.value,
    code = code,
    version = version,
    kind = kind.name,
    params = params,
    isActive = isActive,
    isBaseline = kind == StrategyKind.BASELINE,
)
