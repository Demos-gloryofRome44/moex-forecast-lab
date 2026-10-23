package ru.quotelab.domain

import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.instrument.valueobject.Secid
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset

object TestCandles {
    val SECID = Secid("SBER")
    val START: LocalDate = LocalDate.of(2025, 1, 1)

    fun candle(day: Int, close: Double, secid: Secid = SECID): Candle {
        val date = START.plusDays(day.toLong())
        val price = Price(BigDecimal(close.toString()))
        return Candle(
            secid = secid,
            interval = CandleInterval.DAY,
            beginAt = date.atStartOfDay().toInstant(ZoneOffset.UTC),
            tradeDate = date,
            open = price,
            high = price,
            low = price,
            close = price,
            volume = 1000,
        )
    }

    fun series(closes: List<Double>): List<Candle> = closes.mapIndexed { i, c -> candle(i, c) }

    fun randomWalk(n: Int, seed: Long = 7): List<Candle> {
        val random = java.util.Random(seed)
        var price = 100.0
        return (0 until n).map { i ->
            price *= 1 + (random.nextGaussian() * 0.015)
            candle(i, Math.round(price * 100) / 100.0)
        }
    }
}
