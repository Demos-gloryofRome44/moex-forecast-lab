package ru.quotelab.domain.ml.access

import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.strategy.StrategyResult
import ru.quotelab.domain.strategy.valueobject.Horizon

interface ModelPredictor {
    val modelCode: String
    val modelVersion: String

    fun predict(history: CandleSeries, horizon: Horizon): StrategyResult
}
