package ru.quotelab.restapi.forecast.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Json
data class ForecastBody(
    val id: UUID,
    val strategyId: UUID,
    val strategyCode: String?,
    val strategyVersion: String?,
    val secid: String,
    val horizon: Int,
    val issuedAt: Instant,
    val tradeDate: LocalDate,
    val basePrice: BigDecimal,
    val direction: String,
    val confidence: Double,
    val expectedTargetDate: LocalDate,
    val status: String,
    val origin: String,
    val evaluation: EvaluationBody?,
)

@Json
data class EvaluationBody(
    val evaluatedAt: Instant,
    val targetTradeDate: LocalDate,
    val actualPrice: BigDecimal,
    val actualReturn: BigDecimal,
    val outcome: String,
    val brier: BigDecimal?,
)

@Json
data class ForecastPageBody(
    val items: List<ForecastBody>,
    val page: Int,
    val size: Int,
    val total: Long,
)
