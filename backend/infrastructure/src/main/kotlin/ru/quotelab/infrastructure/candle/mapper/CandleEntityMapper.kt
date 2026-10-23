package ru.quotelab.infrastructure.candle.mapper

import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.infrastructure.candle.CandleEntity
import ru.quotelab.infrastructure.common.toUtc

internal fun CandleEntity.toDomain() = Candle(
    secid = Secid(secid),
    interval = CandleInterval.ofIssCode(interval),
    beginAt = beginAt.toInstant(),
    tradeDate = tradeDate,
    open = Price(open),
    high = Price(high),
    low = Price(low),
    close = Price(close),
    volume = volume,
)

internal fun Candle.toEntity() = CandleEntity(
    secid = secid.value,
    interval = interval.issCode,
    beginAt = beginAt.toUtc(),
    tradeDate = tradeDate,
    open = open.value,
    high = high.value,
    low = low.value,
    close = close.value,
    volume = volume,
)
