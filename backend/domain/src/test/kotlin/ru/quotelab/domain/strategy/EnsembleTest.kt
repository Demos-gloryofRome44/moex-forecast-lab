package ru.quotelab.domain.strategy

import org.junit.jupiter.api.Test
import ru.quotelab.domain.evaluation.Outcome
import ru.quotelab.domain.strategy.ensemble.MultiplicativeWeightsEnsemble
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.StrategyId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnsembleTest {
    private val a = StrategyId.of("a", "1")
    private val b = StrategyId.of("b", "1")
    private val ensemble = MultiplicativeWeightsEnsemble("1", eta = 0.5)

    @Test
    fun `combines member probabilities with weights`() {
        val signals = mapOf(a to Signal(Direction.UP, Confidence(0.8)), b to Signal(Direction.DOWN, Confidence(0.6)))
        val equal = ensemble.combine(signals, ensemble.initialWeights(listOf(a, b)))
        assertEquals(Direction.UP, equal.direction)
        assertEquals(0.6, equal.confidence.value, 1e-12)

        val trustB = ensemble.combine(signals, mapOf(a to 0.1, b to 0.9))
        assertEquals(Direction.DOWN, trustB.direction)
    }

    @Test
    fun `misses shrink weights, hits keep them, weights stay normalised`() {
        val start = ensemble.initialWeights(listOf(a, b))
        val after = ensemble.update(start, mapOf(a to 1.0, b to 0.0))
        assertEquals(1.0, after.values.sum(), 1e-12)
        assertEquals(1.0 / 3, after.getValue(a), 1e-12)
        assertEquals(start, ensemble.update(start, emptyMap()))
    }

    @Test
    fun `abstaining costs as much as a coin flip, so always-flat members cannot collect all the weight`() {
        assertEquals(0.0, ensemble.loss(Direction.UP, Outcome.HIT, marketMoved = true))
        assertEquals(1.0, ensemble.loss(Direction.UP, Outcome.MISS, marketMoved = true))
        assertEquals(0.5, ensemble.loss(Direction.FLAT, Outcome.NEUTRAL, marketMoved = true))
        assertEquals(null, ensemble.loss(Direction.UP, Outcome.NEUTRAL, marketMoved = false))

        var w = ensemble.initialWeights(listOf(a, b))
        repeat(100) { i ->
            val aLoss = if (i % 5 < 3) 0.0 else 1.0
            w = ensemble.update(w, mapOf(a to aLoss, b to MultiplicativeWeightsEnsemble.ABSTAIN_LOSS))
        }
        assertTrue(w.getValue(a) > w.getValue(b), "a=${w[a]} b=${w[b]}")
    }

    @Test
    fun `no votes means flat`() {
        assertEquals(Signal.FLAT, ensemble.combine(emptyMap(), emptyMap()))
    }
}
