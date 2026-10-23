package ru.quotelab.usecase.performance.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class PerformanceQuery(
    val strategyId: UUID,
    val secid: String?,
    val horizon: Int?,
    val from: LocalDate?,
    val till: LocalDate?,
    val origin: String?,
)

data class LeaderboardQuery(
    val horizon: Int,
    val from: LocalDate?,
    val till: LocalDate?,
    val origin: String?,
)

data class HitRateStats(
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

data class PaperTradingStats(
    val trades: Int,
    val totalReturn: BigDecimal,
    val sharpe: Double?,
    val maxDrawdown: BigDecimal,
    val commissionPerSide: BigDecimal,
    val slippagePerSide: BigDecimal,
)

data class PerformanceResponse(
    val strategyId: UUID,
    val strategyCode: String,
    val strategyVersion: String,
    val kind: String,
    val secid: String?,
    val horizon: Int?,
    val origin: String,
    val stats: HitRateStats,
    val paper: PaperTradingStats,
)

data class CalibrationBucket(
    val lower: Double,
    val upper: Double,
    val sampleSize: Int,
    val meanConfidence: Double?,
    val hitRate: Double?,
    val wilsonLow: Double,
    val wilsonHigh: Double,
)

data class CalibrationResponse(
    val strategyId: UUID,
    val horizon: Int?,
    val origin: String,
    val sampleSize: Int,
    val buckets: List<CalibrationBucket>,
)

data class EquityPoint(
    val date: LocalDate,
    val equity: BigDecimal,
    val periodReturn: BigDecimal,
    val trades: Int,
)

data class EquityResponse(
    val strategyId: UUID,
    val secid: String?,
    val horizon: Int,
    val origin: String,
    val trades: Int,
    val points: List<EquityPoint>,
)

data class LeaderboardRow(
    val rank: Int,
    val strategyId: UUID,
    val strategyCode: String,
    val strategyVersion: String,
    val kind: String,
    val isBaseline: Boolean,
    val stats: HitRateStats,
    val paper: PaperTradingStats,
    val hitRateVsBestBaseline: Double?,
)

data class LeaderboardResponse(
    val horizon: Int,
    val origin: String,
    val from: LocalDate?,
    val till: LocalDate?,
    val minSampleSize: Int,
    val rows: List<LeaderboardRow>,
    val warnings: List<String>,
)
