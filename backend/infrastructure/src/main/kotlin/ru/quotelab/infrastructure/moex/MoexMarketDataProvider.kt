package ru.quotelab.infrastructure.moex

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.SecurityInfo
import ru.quotelab.domain.instrument.access.MarketDataProvider
import ru.quotelab.domain.instrument.valueobject.Board
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.problems.MarketDataUnavailableException
import ru.tinkoff.kora.common.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class MoexMarketDataProvider(
    private val client: MoexIssClient,
    private val config: MoexConfig,
) : MarketDataProvider {
    private val log = LoggerFactory.getLogger(javaClass)
    private val throttle = RequestThrottle(config.minRequestInterval())

    override fun fetchCandles(instrument: Instrument, interval: CandleInterval, from: LocalDate, till: LocalDate): List<Candle> {
        val result = mutableListOf<Candle>()
        var start = 0
        while (true) {
            val page = call("candles ${instrument.secid}") {
                client.candles(
                    engine = instrument.board.engine,
                    market = instrument.board.market,
                    board = instrument.board.code,
                    secid = instrument.secid.value,
                    from = from.toString(),
                    till = till.toString(),
                    interval = interval.issCode,
                    start = start,
                )
            }.table("candles").records()
            page.mapNotNullTo(result) { it.toCandle(instrument.secid, interval) }
            if (page.size < config.pageSize()) break
            start += page.size
        }
        return result
    }

    override fun findSecurity(secid: Secid): SecurityInfo? {
        val response = call("security $secid") { client.security(secid.value) }
        val description = response.table("description").records().associate { it.string("name") to it.string("value") }
        if (description.isEmpty()) return null
        val boards = response.table("boards").records()
        val primary = boards.firstOrNull { it.string("is_primary") == "1" } ?: boards.firstOrNull() ?: return null
        val boardId = primary.string("boardid") ?: return null
        val supported = Board.entries.firstOrNull { it.code == boardId }
            ?: boards.firstNotNullOfOrNull { b -> Board.entries.firstOrNull { it.code == b.string("boardid") } }
            ?: throw IllegalArgumentException(
                "Security $secid trades on board $boardId, which is not supported yet (${Board.entries.map { it.code }})",
            )
        val name = description["NAME"] ?: description["SHORTNAME"] ?: secid.value
        return SecurityInfo(secid, supported, name)
    }

    private fun <T> call(what: String, block: () -> T): T {
        throttle.acquire()
        return try {
            block()
        } catch (e: RuntimeException) {
            log.warn("MOEX ISS request '{}' failed: {}", what, e.toString())
            throw MarketDataUnavailableException("MOEX ISS is unavailable: ${e.message ?: e.javaClass.simpleName}", e)
        }
    }

    private fun IssRecord.toCandle(secid: Secid, interval: CandleInterval): Candle? {
        val begin = string("begin")
        val prices = PRICE_COLUMNS.map { column -> decimal(column)?.takeIf { it.signum() > 0 }?.let(::Price) }
        if (begin == null || prices.any { it == null }) return null
        val ohlc = prices.requireNoNulls()
        val beginLocal = LocalDateTime.parse(begin, ISS_DATE_TIME)
        return Candle(
            secid = secid,
            interval = interval,
            beginAt = beginLocal.atZone(MOSCOW).toInstant(),
            tradeDate = beginLocal.toLocalDate(),
            open = ohlc[0],
            high = ohlc[1],
            low = ohlc[2],
            close = ohlc[3],
            volume = decimal("volume")?.toLong() ?: 0L,
        )
    }

    private companion object {
        val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
        val ISS_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val PRICE_COLUMNS = listOf("open", "high", "low", "close")
    }
}
