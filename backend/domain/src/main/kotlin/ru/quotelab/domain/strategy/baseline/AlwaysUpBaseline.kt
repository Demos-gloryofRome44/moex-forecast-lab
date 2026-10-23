package ru.quotelab.domain.strategy.baseline

import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.Strategy
import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.domain.strategy.StrategyResult
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.Horizon

class AlwaysUpBaseline(version: String = "1") : Strategy {
    override val descriptor = StrategyDescriptor(CODE, version, StrategyKind.BASELINE, emptyMap())

    override val requiredHistory: Int = 1

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult =
        if (history.isEmpty()) {
            StrategyResult.InsufficientData(requiredHistory, 0)
        } else {
            StrategyResult.Success(Signal(Direction.UP, Confidence.NEUTRAL))
        }

    companion object {
        const val CODE = "baseline-always-up"
    }
}
