package ru.quotelab.usecase.forecast.dto

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ForecastSearchRequest(
    val secid: String?,
    val strategyId: UUID?,
    val status: String?,
    val horizon: Int?,
    val origin: String?,
    val from: LocalDate?,
    val till: LocalDate?,
    val page: Int,
    val size: Int,
)

data class ForecastResponse(
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
    val evaluation: EvaluationResponse?,
)

data class EvaluationResponse(
    val evaluatedAt: Instant,
    val targetTradeDate: LocalDate,
    val actualPrice: BigDecimal,
    val actualReturn: BigDecimal,
    val outcome: String,
    val brier: BigDecimal?,
)

data class ForecastPageResponse(
    val items: List<ForecastResponse>,
    val page: Int,
    val size: Int,
    val total: Long,
)

data class ForecastRunReport(
    val tradeDates: Map<String, LocalDate>,
    val created: Int,
    val skippedInsufficientData: Int,
)
