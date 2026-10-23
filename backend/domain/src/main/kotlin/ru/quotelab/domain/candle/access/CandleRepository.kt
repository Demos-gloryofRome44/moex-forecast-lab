package ru.quotelab.domain.candle.access

import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.instrument.valueobject.Secid
import java.time.LocalDate

interface CandleRepository {
    fun upsertAll(candles: List<Candle>): Int

    fun findRange(secid: Secid, interval: CandleInterval, from: LocalDate, till: LocalDate): List<Candle>

    fun findHistory(secid: Secid, interval: CandleInterval, tradeDate: LocalDate, limit: Int): List<Candle>

    fun findAfter(secid: Secid, interval: CandleInterval, tradeDate: LocalDate, limit: Int): List<Candle>

    fun findLastTradeDate(secid: Secid, interval: CandleInterval): LocalDate?
}
