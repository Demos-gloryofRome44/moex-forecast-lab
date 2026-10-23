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
import java.util.Objects
import kotlin.random.Random

class RandomBaseline(version: String = "1", private val seed: Long = 42) : Strategy {
    override val descriptor = StrategyDescriptor(CODE, version, StrategyKind.BASELINE, mapOf("seed" to "$seed"))

    override val requiredHistory: Int = 1

    override fun signal(history: CandleSeries, horizon: Horizon): StrategyResult {
        if (history.isEmpty()) return StrategyResult.InsufficientData(requiredHistory, 0)
        val random = Random(Objects.hash(seed, history.secid.value, history.asOf, horizon.days))
        val direction = if (random.nextBoolean()) Direction.UP else Direction.DOWN
        return StrategyResult.Success(Signal(direction, Confidence.NEUTRAL))
    }

    companion object {
        const val CODE = "baseline-random"
    }
}
