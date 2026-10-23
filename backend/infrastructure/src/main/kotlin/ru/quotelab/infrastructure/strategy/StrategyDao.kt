package ru.quotelab.infrastructure.strategy

import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import java.util.UUID

@Repository
interface StrategyDao : JdbcRepository {
    @Query("SELECT id, code, version, kind, params::text AS params, is_active FROM strategies ORDER BY code, version")
    fun findAll(): List<StrategyEntity>

    @Query("SELECT id, code, version, kind, params::text AS params, is_active FROM strategies WHERE id = :id")
    fun findById(id: UUID): StrategyEntity?

    @Query(
        """
        INSERT INTO strategies (id, code, version, kind, params, is_active)
        VALUES (:entity.id, :entity.code, :entity.version, :entity.kind, CAST(:entity.params AS jsonb), :entity.isActive)
        ON CONFLICT (code, version) DO NOTHING
        """,
    )
    fun insert(entity: StrategyEntity)

    @Query("UPDATE strategies SET is_active = :active WHERE id = :id")
    fun setActive(id: UUID, active: Boolean)
}
