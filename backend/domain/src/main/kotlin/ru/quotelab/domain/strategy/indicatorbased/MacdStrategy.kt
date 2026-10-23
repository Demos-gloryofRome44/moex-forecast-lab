package ru.quotelab.domain.strategy.indicatorbased

import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.Strategy
import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.domain.strategy.StrategyResult
import ru.quotelab.domain.strategy.indicator.Indicators
import ru.quotelab.domain.strategy.indicator.confidenceFromStrength
import ru.quotelab.domain.strategy.valueobject.Horizon

class MacdStrategy(
    version: String,
    private val fast: Int = 12,
    private val slow: Int = 26,
    private val signalPeriod: Int = 9,
) : Strategy {
    override val descriptor = StrategyDescriptor(
        code = CODE,
        version = version,
        kind = StrategyKind.INDICATOR,
        params = mapOf("fast" to "$fast", "slow" to "$slow", "signal" to "$signalPeriod"),
    )

    override val requiredHistory: Int = (slow + signalPeriod) * 2

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult {
        if (history.size < requiredHistory) return StrategyResult.InsufficientData(requiredHistory, history.size)
        val point = Indicators.macd(history.closes, fast, slow, signalPeriod).last()!!
        val strength = point.histogram.divide(history.last.close.value, Indicators.MC).toDouble()
        return StrategyResult.Success(
            Signal(Direction.ofSign(strength.compareTo(0.0)), confidenceFromStrength(strength, STRENGTH_SCALE)),
        )
    }

    companion object {
        const val CODE = "macd"
        private const val STRENGTH_SCALE = 0.005
    }
}
