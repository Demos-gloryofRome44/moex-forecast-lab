package ru.quotelab.infrastructure.forecast

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import java.time.LocalDate
import java.util.UUID

@Repository
interface ForecastDao : JdbcRepository {
    @Query(
        """
        INSERT INTO forecasts (id, strategy_id, secid, horizon_days, issued_at, trade_date, base_price,
                               direction, confidence, expected_target_date, status, origin)
        VALUES (:f.id, :f.strategyId, :f.secid, :f.horizonDays, :f.issuedAt, :f.tradeDate, :f.basePrice,
                :f.direction, :f.confidence, :f.expectedTargetDate, :f.status, :f.origin)
        ON CONFLICT (strategy_id, secid, horizon_days, trade_date, origin) DO NOTHING
        """,
    )
    fun insertIfAbsent(f: ForecastEntity): UpdateCount

    @Query("SELECT $COLUMNS FROM forecasts WHERE id = :id")
    fun findById(id: UUID): ForecastEntity?

    @Query(
        """
        SELECT $COLUMNS FROM forecasts
         WHERE status = 'PENDING' AND origin = :origin AND (CAST(:afterId AS uuid) IS NULL OR id > :afterId)
         ORDER BY id
         LIMIT :limit
        """,
    )
    fun findPending(origin: String, limit: Int, afterId: UUID?): List<ForecastEntity>

    @Query("UPDATE forecasts SET status = 'EVALUATED' WHERE id = :id AND status = 'PENDING'")
    fun markEvaluated(id: UUID): UpdateCount

    @Query(
        """
        SELECT $COLUMNS FROM forecasts
         WHERE $FILTER
         ORDER BY trade_date DESC, issued_at DESC, id
         LIMIT :limit OFFSET :offset
        """,
    )
    fun search(
        secid: String?,
        strategyId: UUID?,
        status: String?,
        horizon: Int?,
        origin: String?,
        from: LocalDate?,
        till: LocalDate?,
        limit: Int,
        offset: Long,
    ): List<ForecastEntity>

    @Query("SELECT count(*) FROM forecasts WHERE $FILTER")
    fun count(
        secid: String?,
        strategyId: UUID?,
        status: String?,
        horizon: Int?,
        origin: String?,
        from: LocalDate?,
        till: LocalDate?,
    ): Long

    companion object {
        const val COLUMNS = "id, strategy_id, secid, horizon_days, issued_at, trade_date, base_price, " +
            "direction, confidence, expected_target_date, status, origin"

        const val FILTER = "(CAST(:secid AS text) IS NULL OR secid = :secid) " +
            "AND (CAST(:strategyId AS uuid) IS NULL OR strategy_id = :strategyId) " +
            "AND (CAST(:status AS text) IS NULL OR status = :status) " +
            "AND (CAST(:horizon AS int) IS NULL OR horizon_days = :horizon) " +
            "AND (CAST(:origin AS text) IS NULL OR origin = :origin) " +
            "AND (CAST(:from AS date) IS NULL OR trade_date >= :from) " +
            "AND (CAST(:till AS date) IS NULL OR trade_date <= :till)"
    }
}
