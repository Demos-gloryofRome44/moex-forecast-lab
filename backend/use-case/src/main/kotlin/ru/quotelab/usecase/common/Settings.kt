package ru.quotelab.usecase.common

import ru.quotelab.domain.strategy.valueobject.Horizon
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class MarketSettings(
    val zone: ZoneId = ZoneId.of("Europe/Moscow"),
    val initialLoadYears: Long = 2,
    val includeWeekendSessions: Boolean = false,
) {
    fun endOfDay(date: LocalDate): Instant = date.atTime(LocalTime.of(23, 59, 59)).atZone(zone).toInstant()
}

data class ForecastSettings(
    val horizons: List<Horizon> = listOf(Horizon(1), Horizon(5), Horizon(20)),
    val maxStaleDays: Long = 5,
)

data class EvaluationSettings(
    val flatThreshold: BigDecimal = BigDecimal("0.001"),
    val batchSize: Int = 1000,
)

data class PaperTradingSettings(
    val commissionPerSide: BigDecimal = BigDecimal("0.0005"),
    val slippagePerSide: BigDecimal = BigDecimal("0.0005"),
)

data class MetricsSettings(
    val minSampleSize: Int = 30,
    val confidenceLevelZ: Double = 1.96,
)
