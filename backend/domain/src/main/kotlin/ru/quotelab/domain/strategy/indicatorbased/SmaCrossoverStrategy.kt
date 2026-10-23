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

class SmaCrossoverStrategy(
    version: String,
    private val fast: Int = 10,
    private val slow: Int = 30,
) : Strategy {
    init {
        require(fast in 1 until slow) { "SMA crossover requires 0 < fast < slow" }
    }

    override val descriptor = StrategyDescriptor(
        code = CODE,
        version = version,
        kind = StrategyKind.INDICATOR,
        params = mapOf("fast" to "$fast", "slow" to "$slow"),
    )

    override val requiredHistory: Int = slow

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult {
        if (history.size < requiredHistory) return StrategyResult.InsufficientData(requiredHistory, history.size)
        val closes = history.closes
        val fastSma = Indicators.sma(closes, fast).last()!!
        val slowSma = Indicators.sma(closes, slow).last()!!
        val gap = (fastSma - slowSma).divide(slowSma, Indicators.MC).toDouble()
        return StrategyResult.Success(
            Signal(Direction.ofSign(gap.compareTo(0.0)), confidenceFromStrength(gap, STRENGTH_SCALE)),
        )
    }

    companion object {
        const val CODE = "sma-crossover"
        private const val STRENGTH_SCALE = 0.03
    }
}
