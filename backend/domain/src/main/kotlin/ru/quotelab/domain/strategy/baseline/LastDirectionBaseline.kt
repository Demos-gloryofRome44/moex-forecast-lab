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

class LastDirectionBaseline(version: String = "1") : Strategy {
    override val descriptor = StrategyDescriptor(CODE, version, StrategyKind.BASELINE, emptyMap())

    override val requiredHistory: Int = 2

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult {
        if (history.size < requiredHistory) return StrategyResult.InsufficientData(requiredHistory, history.size)
        val last = history[history.size - 1].close
        val prev = history[history.size - 2].close
        return StrategyResult.Success(Signal(Direction.ofSign(last.compareTo(prev)), Confidence.NEUTRAL))
    }

    companion object {
        const val CODE = "baseline-last-direction"
    }
}
