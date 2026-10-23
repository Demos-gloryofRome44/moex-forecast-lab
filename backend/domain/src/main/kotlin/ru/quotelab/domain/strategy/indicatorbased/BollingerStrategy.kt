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
import java.math.BigDecimal

class BollingerStrategy(
    version: String,
    private val period: Int = 20,
    private val width: BigDecimal = BigDecimal("2.0"),
) : Strategy {
    override val descriptor = StrategyDescriptor(
        code = CODE,
        version = version,
        kind = StrategyKind.INDICATOR,
        params = mapOf("period" to "$period", "width" to width.toPlainString()),
    )

    override val requiredHistory: Int = period

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult {
        if (history.size < requiredHistory) return StrategyResult.InsufficientData(requiredHistory, history.size)
        val band = Indicators.bollinger(history.closes, period, width).last()!!
        val close = history.last.close.value
        val halfWidth = band.upper - band.middle
        if (halfWidth.signum() == 0) return StrategyResult.Success(Signal.FLAT)
        val signal = when {
            close < band.lower -> Signal(Direction.UP, confidenceFromStrength(excess(band.lower - close, halfWidth), STRENGTH_SCALE))
            close > band.upper -> Signal(Direction.DOWN, confidenceFromStrength(excess(close - band.upper, halfWidth), STRENGTH_SCALE))
            else -> Signal.FLAT
        }
        return StrategyResult.Success(signal)
    }

    private fun excess(distance: BigDecimal, halfWidth: BigDecimal): Double =
        distance.divide(halfWidth, Indicators.MC).toDouble()

    companion object {
        const val CODE = "bollinger"
        private const val STRENGTH_SCALE = 0.5
    }
}
