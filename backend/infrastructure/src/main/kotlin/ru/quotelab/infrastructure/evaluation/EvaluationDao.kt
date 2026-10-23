package ru.quotelab.infrastructure.evaluation

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import java.time.LocalDate
import java.time.OffsetDateTime

@Repository
interface EvaluationDao : JdbcRepository {
    @Query(
        """
        INSERT INTO evaluations (forecast_id, evaluated_at, target_trade_date, actual_price, actual_return, outcome, brier)
        VALUES (:e.forecastId, :e.evaluatedAt, :e.targetTradeDate, :e.actualPrice, :e.actualReturn, :e.outcome, :e.brier)
        ON CONFLICT (forecast_id) DO NOTHING
        """,
    )
    fun insertIfAbsent(e: EvaluationEntity): UpdateCount

    @Query(
        """
        SELECT forecast_id, evaluated_at, target_trade_date, actual_price, actual_return, outcome, brier
          FROM evaluations
         WHERE forecast_id = ANY(string_to_array(:ids, ',')::uuid[])
        """,
    )
    fun findByForecastIds(ids: String): List<EvaluationEntity>

    @Query(
        """
        SELECT f.id, f.strategy_id, f.secid, f.horizon_days, f.issued_at, f.trade_date, f.base_price,
               f.direction, f.confidence, f.expected_target_date, f.status, f.origin,
               e.evaluated_at, e.target_trade_date, e.actual_price, e.actual_return, e.outcome, e.brier
          FROM evaluations e
          JOIN forecasts f ON f.id = e.forecast_id
         WHERE f.origin = :origin
           AND (CAST(:strategyIds AS text) IS NULL OR f.strategy_id = ANY(string_to_array(:strategyIds, ',')::uuid[]))
           AND (CAST(:secid AS text) IS NULL OR f.secid = :secid)
           AND (CAST(:horizon AS int) IS NULL OR f.horizon_days = :horizon)
           AND (CAST(:from AS date) IS NULL OR f.trade_date >= :from)
           AND (CAST(:till AS date) IS NULL OR f.trade_date <= :till)
           AND (CAST(:evaluatedAfter AS timestamptz) IS NULL OR e.evaluated_at > :evaluatedAfter)
           AND (CAST(:evaluatedUpTo AS timestamptz) IS NULL OR e.evaluated_at <= :evaluatedUpTo)
         ORDER BY e.evaluated_at, e.target_trade_date, f.id
        """,
    )
    fun findEvaluated(
        origin: String,
        strategyIds: String?,
        secid: String?,
        horizon: Int?,
        from: LocalDate?,
        till: LocalDate?,
        evaluatedAfter: OffsetDateTime?,
        evaluatedUpTo: OffsetDateTime?,
    ): List<EvaluatedRow>
}
