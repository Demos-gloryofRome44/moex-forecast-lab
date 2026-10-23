package ru.quotelab.infrastructure.forecast

import ru.tinkoff.kora.database.jdbc.EntityJdbc
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@EntityJdbc
data class ForecastEntity(
    val id: UUID,
    val strategyId: UUID,
    val secid: String,
    val horizonDays: Int,
    val issuedAt: OffsetDateTime,
    val tradeDate: LocalDate,
    val basePrice: BigDecimal,
    val direction: String,
    val confidence: Double,
    val expectedTargetDate: LocalDate,
    val status: String,
    val origin: String,
)
