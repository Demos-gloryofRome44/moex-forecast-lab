package ru.quotelab.usecase.candle.mapper

import ru.quotelab.domain.candle.Candle
import ru.quotelab.usecase.candle.dto.CandleResponse

internal fun Candle.toResponse() = CandleResponse(
    tradeDate = tradeDate,
    beginAt = beginAt,
    open = open.value,
    high = high.value,
    low = low.value,
    close = close.value,
    volume = volume,
)
