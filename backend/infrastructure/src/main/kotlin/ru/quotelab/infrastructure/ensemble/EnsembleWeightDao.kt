package ru.quotelab.infrastructure.ensemble

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface EnsembleWeightDao : JdbcRepository {
    @Query(
        """
        INSERT INTO ensemble_weights (ensemble_id, member_strategy_id, horizon_days, origin, effective_at, weight)
        VALUES (:w.ensembleId, :w.memberStrategyId, :w.horizonDays, :w.origin, :w.effectiveAt, :w.weight)
        ON CONFLICT (ensemble_id, member_strategy_id, horizon_days, origin, effective_at) DO NOTHING
        """,
    )
    fun append(@Batch w: List<EnsembleWeightEntity>): UpdateCount

    @Query(
        """
        SELECT ensemble_id, member_strategy_id, horizon_days, origin, effective_at, weight
          FROM ensemble_weights
         WHERE ensemble_id = :ensembleId AND horizon_days = :horizon AND origin = :origin
           AND effective_at = (
               SELECT max(effective_at) FROM ensemble_weights
                WHERE ensemble_id = :ensembleId AND horizon_days = :horizon AND origin = :origin
                  AND effective_at <= :asOf)
        """,
    )
    fun findEffective(ensembleId: UUID, horizon: Int, origin: String, asOf: OffsetDateTime): List<EnsembleWeightEntity>

    @Query(
        """
        SELECT ensemble_id, member_strategy_id, horizon_days, origin, effective_at, weight
          FROM ensemble_weights
         WHERE ensemble_id = :ensembleId AND horizon_days = :horizon AND origin = :origin
         ORDER BY effective_at, member_strategy_id
        """,
    )
    fun findHistory(ensembleId: UUID, horizon: Int, origin: String): List<EnsembleWeightEntity>
}
