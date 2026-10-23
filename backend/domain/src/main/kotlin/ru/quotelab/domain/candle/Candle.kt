package ru.quotelab.domain.candle

import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.instrument.valueobject.Secid
import java.time.Instant
import java.time.LocalDate

data class Candle(
    val secid: Secid,
    val interval: CandleInterval,
    val beginAt: Instant,
    val tradeDate: LocalDate,
    val open: Price,
    val high: Price,
    val low: Price,
    val close: Price,
    val volume: Long,
)
