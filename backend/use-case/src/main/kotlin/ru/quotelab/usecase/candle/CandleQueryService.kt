package ru.quotelab.usecase.candle

import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.problems.InstrumentNotFoundException
import ru.quotelab.usecase.candle.dto.CandleQuery
import ru.quotelab.usecase.candle.dto.CandleResponse
import ru.quotelab.usecase.candle.mapper.toResponse
import ru.quotelab.usecase.common.requireValidRange
import java.time.Clock
import java.time.LocalDate

class CandleQueryService(
    private val instruments: InstrumentRepository,
    private val candles: CandleRepository,
    private val clock: Clock,
) {
    fun find(query: CandleQuery): List<CandleResponse> {
        requireValidRange(query.from, query.till)
        val secid = Secid.of(query.secid)
        instruments.findBySecid(secid) ?: throw InstrumentNotFoundException(secid.value)
        val interval = query.interval?.let(CandleInterval::ofIssCode) ?: CandleInterval.DAY
        val till = query.till ?: LocalDate.now(clock)
        val from = query.from ?: till.minusYears(1)
        return candles.findRange(secid, interval, from, till).map { it.toResponse() }
    }
}
