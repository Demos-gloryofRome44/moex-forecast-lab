package ru.quotelab.usecase.instrument

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.access.MarketDataProvider
import ru.quotelab.domain.instrument.valueobject.Board
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.problems.SecurityNotFoundException
import ru.quotelab.usecase.instrument.dto.AddInstrumentRequest
import ru.quotelab.usecase.instrument.dto.InstrumentResponse
import ru.quotelab.usecase.instrument.mapper.toResponse

class InstrumentService(
    private val instruments: InstrumentRepository,
    private val candles: CandleRepository,
    private val marketData: MarketDataProvider,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findAll(): List<InstrumentResponse> =
        instruments.findAll().map { it.toResponse(candles.findLastTradeDate(it.secid, CandleInterval.DAY)) }

    fun add(request: AddInstrumentRequest): InstrumentResponse {
        val secid = Secid.of(request.secid)
        val requestedBoard = request.board?.let(Board::of)
        val existing = instruments.findBySecid(secid)
        if (existing != null) {
            val saved = if (existing.isActive) existing else instruments.save(existing.copy(isActive = true))
            return saved.toResponse(candles.findLastTradeDate(secid, CandleInterval.DAY))
        }
        val info = marketData.findSecurity(secid) ?: throw SecurityNotFoundException(secid.value)
        val instrument = Instrument(
            secid = info.secid,
            board = requestedBoard ?: info.board,
            name = info.name,
            isActive = true,
        )
        log.info("Adding instrument {} ({}) on {}", instrument.secid, instrument.name, instrument.board)
        return instruments.save(instrument).toResponse(null)
    }

    fun seed(watchlist: List<Instrument>) {
        watchlist.filter { instruments.findBySecid(it.secid) == null }.forEach {
            log.info("Seeding instrument {}", it.secid)
            instruments.save(it)
        }
    }
}
