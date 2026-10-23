package ru.quotelab.domain.candle

import ru.quotelab.domain.instrument.valueobject.Secid
import java.math.BigDecimal
import java.time.LocalDate

class CandleSeries private constructor(
    val secid: Secid,
    val asOf: LocalDate,
    private val candles: List<Candle>,
) {
    val size: Int get() = candles.size

    val last: Candle get() = candles.last()

    val closes: List<BigDecimal> by lazy { candles.map { it.close.value } }

    fun isEmpty(): Boolean = candles.isEmpty()

    operator fun get(index: Int): Candle = candles[index]

    fun toList(): List<Candle> = candles

    companion object {
        fun asOf(secid: Secid, tradeDate: LocalDate, history: Iterable<Candle>): CandleSeries {
            val visible = history
                .filter { it.secid == secid && !it.tradeDate.isAfter(tradeDate) }
                .sortedBy { it.tradeDate }
            return CandleSeries(secid, tradeDate, visible.toList())
        }
    }
}
