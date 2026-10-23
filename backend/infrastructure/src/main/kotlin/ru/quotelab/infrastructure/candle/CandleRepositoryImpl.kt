package ru.quotelab.infrastructure.candle

import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.infrastructure.candle.mapper.toDomain
import ru.quotelab.infrastructure.candle.mapper.toEntity
import ru.tinkoff.kora.common.Component
import java.time.LocalDate

@Component
class CandleRepositoryImpl(
    private val dao: CandleDao,
) : CandleRepository {
    override fun upsertAll(candles: List<Candle>): Int =
        candles.chunked(BATCH_SIZE).sumOf { chunk -> dao.upsert(chunk.map { it.toEntity() }).value.toInt() }

    override fun findRange(secid: Secid, interval: CandleInterval, from: LocalDate, till: LocalDate): List<Candle> =
        dao.findRange(secid.value, interval.issCode, from, till).map { it.toDomain() }

    override fun findHistory(secid: Secid, interval: CandleInterval, tradeDate: LocalDate, limit: Int): List<Candle> =
        dao.findHistory(secid.value, interval.issCode, tradeDate, limit).map { it.toDomain() }

    override fun findAfter(secid: Secid, interval: CandleInterval, tradeDate: LocalDate, limit: Int): List<Candle> =
        dao.findAfter(secid.value, interval.issCode, tradeDate, limit).map { it.toDomain() }

    override fun findLastTradeDate(secid: Secid, interval: CandleInterval): LocalDate? =
        dao.findLastTradeDate(secid.value, interval.issCode)

    private companion object {
        const val BATCH_SIZE = 500
    }
}
