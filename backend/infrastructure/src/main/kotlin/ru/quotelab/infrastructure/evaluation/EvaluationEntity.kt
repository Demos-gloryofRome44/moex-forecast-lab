package ru.quotelab.infrastructure.evaluation

import ru.tinkoff.kora.database.jdbc.EntityJdbc
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@EntityJdbc
data class EvaluationEntity(
    val forecastId: UUID,
    val evaluatedAt: OffsetDateTime,
    val targetTradeDate: LocalDate,
    val actualPrice: BigDecimal,
    val actualReturn: BigDecimal,
    val outcome: String,
    val brier: BigDecimal?,
)

@EntityJdbc
data class EvaluatedRow(
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
    val evaluatedAt: OffsetDateTime,
    val targetTradeDate: LocalDate,
    val actualPrice: BigDecimal,
    val actualReturn: BigDecimal,
    val outcome: String,
    val brier: BigDecimal?,
)
