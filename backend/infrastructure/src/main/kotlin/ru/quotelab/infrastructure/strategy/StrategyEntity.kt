package ru.quotelab.infrastructure.strategy

import ru.tinkoff.kora.database.jdbc.EntityJdbc
import java.util.UUID

@EntityJdbc
data class StrategyEntity(
    val id: UUID,
    val code: String,
    val version: String,
    val kind: String,
    val params: String,
    val isActive: Boolean,
)
