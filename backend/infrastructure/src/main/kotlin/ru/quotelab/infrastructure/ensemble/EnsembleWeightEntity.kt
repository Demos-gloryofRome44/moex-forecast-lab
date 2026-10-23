package ru.quotelab.infrastructure.ensemble

import ru.tinkoff.kora.database.jdbc.EntityJdbc
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@EntityJdbc
data class EnsembleWeightEntity(
    val ensembleId: UUID,
    val memberStrategyId: UUID,
    val horizonDays: Int,
    val origin: String,
    val effectiveAt: OffsetDateTime,
    val weight: BigDecimal,
)
