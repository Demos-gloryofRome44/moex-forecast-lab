package ru.quotelab.infrastructure.strategy.mapper

import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.infrastructure.common.FlatJson
import ru.quotelab.infrastructure.strategy.StrategyEntity

internal fun StrategyEntity.toDomain() = StrategyDescriptor(
    code = code,
    version = version,
    kind = StrategyKind.valueOf(kind),
    params = FlatJson.read(params),
    isActive = isActive,
)

internal fun StrategyDescriptor.toEntity() = StrategyEntity(
    id = id.value,
    code = code,
    version = version,
    kind = kind.name,
    params = FlatJson.write(params),
    isActive = isActive,
)
