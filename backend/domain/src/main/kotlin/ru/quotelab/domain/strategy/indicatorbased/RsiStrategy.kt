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

class RsiStrategy(
    version: String,
    private val period: Int = 14,
    private val oversold: Double = 30.0,
    private val overbought: Double = 70.0,
) : Strategy {
    init {
        require(oversold < overbought) { "RSI oversold level must be below overbought level" }
    }

    override val descriptor = StrategyDescriptor(
        code = CODE,
        version = version,
        kind = StrategyKind.INDICATOR,
        params = mapOf("period" to "$period", "oversold" to "$oversold", "overbought" to "$overbought"),
    )

    override val requiredHistory: Int = period * WARMUP_FACTOR + 1

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult {
        if (history.size < requiredHistory) return StrategyResult.InsufficientData(requiredHistory, history.size)
        val rsi = Indicators.rsi(history.closes, period).last()!!.toDouble()
        val signal = when {
            rsi < oversold -> Signal(Direction.UP, confidenceFromStrength(oversold - rsi, STRENGTH_SCALE))
            rsi > overbought -> Signal(Direction.DOWN, confidenceFromStrength(rsi - overbought, STRENGTH_SCALE))
            else -> Signal.FLAT
        }
        return StrategyResult.Success(signal)
    }

    companion object {
        const val CODE = "rsi"
        private const val STRENGTH_SCALE = 10.0

        private const val WARMUP_FACTOR = 3
    }
}
