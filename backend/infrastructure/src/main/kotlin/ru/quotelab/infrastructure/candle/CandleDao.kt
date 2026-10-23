package ru.quotelab.infrastructure.candle

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import java.time.LocalDate

@Repository
interface CandleDao : JdbcRepository {
    @Query(
        """
        INSERT INTO candles (secid, interval, begin_at, trade_date, open, high, low, close, volume)
        VALUES (:candle.secid, :candle.interval, :candle.beginAt, :candle.tradeDate,
                :candle.open, :candle.high, :candle.low, :candle.close, :candle.volume)
        ON CONFLICT (secid, interval, begin_at) DO UPDATE
           SET open = EXCLUDED.open, high = EXCLUDED.high, low = EXCLUDED.low,
               close = EXCLUDED.close, volume = EXCLUDED.volume, trade_date = EXCLUDED.trade_date
        """,
    )
    fun upsert(@Batch candle: List<CandleEntity>): UpdateCount

    @Query(
        """
        SELECT secid, interval, begin_at, trade_date, open, high, low, close, volume
          FROM candles
         WHERE secid = :secid AND interval = :interval AND trade_date BETWEEN :from AND :till
         ORDER BY trade_date
        """,
    )
    fun findRange(secid: String, interval: Int, from: LocalDate, till: LocalDate): List<CandleEntity>

    @Query(
        """
        SELECT * FROM (
            SELECT secid, interval, begin_at, trade_date, open, high, low, close, volume
              FROM candles
             WHERE secid = :secid AND interval = :interval AND trade_date <= :tradeDate
             ORDER BY trade_date DESC
             LIMIT :limit
        ) recent ORDER BY trade_date
        """,
    )
    fun findHistory(secid: String, interval: Int, tradeDate: LocalDate, limit: Int): List<CandleEntity>

    @Query(
        """
        SELECT secid, interval, begin_at, trade_date, open, high, low, close, volume
          FROM candles
         WHERE secid = :secid AND interval = :interval AND trade_date > :tradeDate
         ORDER BY trade_date
         LIMIT :limit
        """,
    )
    fun findAfter(secid: String, interval: Int, tradeDate: LocalDate, limit: Int): List<CandleEntity>

    @Query("SELECT max(trade_date) FROM candles WHERE secid = :secid AND interval = :interval")
    fun findLastTradeDate(secid: String, interval: Int): LocalDate?
}
