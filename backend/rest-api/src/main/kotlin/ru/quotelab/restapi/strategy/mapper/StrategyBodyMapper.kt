package ru.quotelab.restapi.strategy.mapper

import ru.quotelab.restapi.strategy.dto.StrategyBody
import ru.quotelab.usecase.strategy.dto.StrategyResponse

internal fun StrategyResponse.toBody() = StrategyBody(
    id = id,
    code = code,
    version = version,
    kind = kind,
    params = params,
    isActive = isActive,
    isBaseline = isBaseline,
)
