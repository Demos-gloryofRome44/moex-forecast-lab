package ru.quotelab.restapi.performance.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Json
data class HitRateStatsBody(
    val evaluated: Int,
    val sampleSize: Int,
    val hits: Int,
    val misses: Int,
    val neutrals: Int,
    val hitRate: Double?,
    val wilsonLow: Double,
    val wilsonHigh: Double,
    val meanBrier: Double?,
    val insufficientData: Boolean,
    val beatsCoinFlip: Boolean,
)

@Json
data class PaperTradingStatsBody(
    val trades: Int,
    val totalReturn: BigDecimal,
    val sharpe: Double?,
    val maxDrawdown: BigDecimal,
    val commissionPerSide: BigDecimal,
    val slippagePerSide: BigDecimal,
)

@Json
data class PerformanceBody(
    val strategyId: UUID,
    val strategyCode: String,
    val strategyVersion: String,
    val kind: String,
    val secid: String?,
    val horizon: Int?,
    val origin: String,
    val stats: HitRateStatsBody,
    val paper: PaperTradingStatsBody,
)

@Json
data class CalibrationBucketBody(
    val lower: Double,
    val upper: Double,
    val sampleSize: Int,
    val meanConfidence: Double?,
    val hitRate: Double?,
    val wilsonLow: Double,
    val wilsonHigh: Double,
)

@Json
data class CalibrationBody(
    val strategyId: UUID,
    val horizon: Int?,
    val origin: String,
    val sampleSize: Int,
    val buckets: List<CalibrationBucketBody>,
)

@Json
data class EquityPointBody(
    val date: LocalDate,
    val equity: BigDecimal,
    val periodReturn: BigDecimal,
    val trades: Int,
)

@Json
data class EquityBody(
    val strategyId: UUID,
    val secid: String?,
    val horizon: Int,
    val origin: String,
    val trades: Int,
    val points: List<EquityPointBody>,
)

@Json
data class LeaderboardRowBody(
    val rank: Int,
    val strategyId: UUID,
    val strategyCode: String,
    val strategyVersion: String,
    val kind: String,
    val isBaseline: Boolean,
    val stats: HitRateStatsBody,
    val paper: PaperTradingStatsBody,
    val hitRateVsBestBaseline: Double?,
)

@Json
data class LeaderboardBody(
    val horizon: Int,
    val origin: String,
    val from: LocalDate?,
    val till: LocalDate?,
    val minSampleSize: Int,
    val rows: List<LeaderboardRowBody>,
    val warnings: List<String>,
)
