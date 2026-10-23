package ru.quotelab.infrastructure.instrument

import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository

@Repository
interface InstrumentDao : JdbcRepository {
    @Query("SELECT secid, board, name, is_active FROM instruments ORDER BY secid")
    fun findAll(): List<InstrumentEntity>

    @Query("SELECT secid, board, name, is_active FROM instruments WHERE is_active ORDER BY secid")
    fun findActive(): List<InstrumentEntity>

    @Query("SELECT secid, board, name, is_active FROM instruments WHERE secid = :secid")
    fun findBySecid(secid: String): InstrumentEntity?

    @Query(
        """
        INSERT INTO instruments (secid, board, name, is_active)
        VALUES (:entity.secid, :entity.board, :entity.name, :entity.isActive)
        ON CONFLICT (secid) DO UPDATE SET board = EXCLUDED.board, name = EXCLUDED.name, is_active = EXCLUDED.is_active
        """,
    )
    fun upsert(entity: InstrumentEntity)
}
