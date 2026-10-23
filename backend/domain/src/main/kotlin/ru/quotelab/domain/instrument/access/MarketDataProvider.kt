package ru.quotelab.domain.instrument.access

import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.SecurityInfo
import ru.quotelab.domain.instrument.valueobject.Secid
import java.time.LocalDate

interface MarketDataProvider {
    fun fetchCandles(instrument: Instrument, interval: CandleInterval, from: LocalDate, till: LocalDate): List<Candle>

    fun findSecurity(secid: Secid): SecurityInfo?
}
