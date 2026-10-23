package ru.quotelab.domain.strategy.ensemble

import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import java.math.BigDecimal
import java.time.Instant

data class EnsembleWeight(
    val ensembleId: StrategyId,
    val memberId: StrategyId,
    val horizon: Horizon,
    val origin: ForecastOrigin,
    val effectiveAt: Instant,
    val weight: BigDecimal,
)
