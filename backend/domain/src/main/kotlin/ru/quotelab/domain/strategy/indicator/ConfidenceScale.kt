package ru.quotelab.domain.strategy.indicator

import ru.quotelab.domain.strategy.valueobject.Confidence
import kotlin.math.abs
import kotlin.math.tanh

internal fun confidenceFromStrength(strength: Double, scale: Double): Confidence =
    Confidence.clamped(0.5 + MAX_EXTRA * tanh(abs(strength) / scale))

private const val MAX_EXTRA = 0.4
