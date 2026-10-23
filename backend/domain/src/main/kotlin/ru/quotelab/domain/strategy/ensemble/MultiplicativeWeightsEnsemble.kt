package ru.quotelab.domain.strategy.ensemble

import ru.quotelab.domain.evaluation.Outcome
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.StrategyId
import kotlin.math.pow

class MultiplicativeWeightsEnsemble(
    version: String,
    val eta: Double = 0.1,
) {
    init {
        require(eta > 0.0 && eta < 1.0) { "Ensemble learning rate must be within (0, 1)" }
    }

    val descriptor = StrategyDescriptor(
        code = CODE,
        version = version,
        kind = StrategyKind.ENSEMBLE,
        params = mapOf("eta" to "$eta", "update" to "hedge", "abstainLoss" to "$ABSTAIN_LOSS"),
    )

    val id: StrategyId get() = descriptor.id

    fun initialWeights(members: Collection<StrategyId>): Map<StrategyId, Double> =
        members.associateWith { 1.0 / members.size }

    fun combine(signals: Map<StrategyId, Signal>, weights: Map<StrategyId, Double>): Signal {
        val voting = signals.filterKeys { (weights[it] ?: 0.0) > 0.0 }
        val totalWeight = voting.keys.sumOf { weights.getValue(it) }
        if (voting.isEmpty() || totalWeight <= 0.0) return Signal.FLAT
        val pUp = voting.entries.sumOf { (id, signal) -> weights.getValue(id) * probabilityUp(signal) } / totalWeight
        return when {
            pUp > 0.5 + EPS -> Signal(Direction.UP, Confidence.clamped(pUp))
            pUp < 0.5 - EPS -> Signal(Direction.DOWN, Confidence.clamped(1 - pUp))
            else -> Signal.FLAT
        }
    }

    fun update(weights: Map<StrategyId, Double>, losses: Map<StrategyId, Double>): Map<StrategyId, Double> {
        val raw = weights.mapValues { (id, w) -> losses[id]?.let { w * (1 - eta).pow(it) } ?: w }
        val total = raw.values.sum()
        return if (total > 0.0) raw.mapValues { it.value / total } else weights
    }

    fun loss(direction: Direction, outcome: Outcome, marketMoved: Boolean): Double? = when {
        outcome == Outcome.HIT -> 0.0
        outcome == Outcome.MISS -> 1.0
        direction == Direction.FLAT && marketMoved -> ABSTAIN_LOSS
        else -> null
    }

    private fun probabilityUp(signal: Signal): Double = when (signal.direction) {
        Direction.UP -> signal.confidence.value
        Direction.DOWN -> 1 - signal.confidence.value
        Direction.FLAT -> 0.5
    }

    companion object {
        const val CODE = "ensemble-mw"
        const val ABSTAIN_LOSS = 0.5
        private const val EPS = 1e-9
    }
}
