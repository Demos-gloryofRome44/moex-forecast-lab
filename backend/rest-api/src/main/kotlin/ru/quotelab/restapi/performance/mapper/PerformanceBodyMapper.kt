package ru.quotelab.restapi.performance.mapper

import ru.quotelab.restapi.performance.dto.CalibrationBody
import ru.quotelab.restapi.performance.dto.CalibrationBucketBody
import ru.quotelab.restapi.performance.dto.EquityBody
import ru.quotelab.restapi.performance.dto.EquityPointBody
import ru.quotelab.restapi.performance.dto.HitRateStatsBody
import ru.quotelab.restapi.performance.dto.LeaderboardBody
import ru.quotelab.restapi.performance.dto.LeaderboardRowBody
import ru.quotelab.restapi.performance.dto.PaperTradingStatsBody
import ru.quotelab.restapi.performance.dto.PerformanceBody
import ru.quotelab.usecase.performance.dto.CalibrationResponse
import ru.quotelab.usecase.performance.dto.EquityResponse
import ru.quotelab.usecase.performance.dto.HitRateStats
import ru.quotelab.usecase.performance.dto.LeaderboardResponse
import ru.quotelab.usecase.performance.dto.PaperTradingStats
import ru.quotelab.usecase.performance.dto.PerformanceResponse

internal fun HitRateStats.toBody() = HitRateStatsBody(
    evaluated = evaluated,
    sampleSize = sampleSize,
    hits = hits,
    misses = misses,
    neutrals = neutrals,
    hitRate = hitRate,
    wilsonLow = wilsonLow,
    wilsonHigh = wilsonHigh,
    meanBrier = meanBrier,
    insufficientData = insufficientData,
    beatsCoinFlip = beatsCoinFlip,
)

internal fun PaperTradingStats.toBody() = PaperTradingStatsBody(
    trades = trades,
    totalReturn = totalReturn,
    sharpe = sharpe,
    maxDrawdown = maxDrawdown,
    commissionPerSide = commissionPerSide,
    slippagePerSide = slippagePerSide,
)

internal fun PerformanceResponse.toBody() = PerformanceBody(
    strategyId = strategyId,
    strategyCode = strategyCode,
    strategyVersion = strategyVersion,
    kind = kind,
    secid = secid,
    horizon = horizon,
    origin = origin,
    stats = stats.toBody(),
    paper = paper.toBody(),
)

internal fun CalibrationResponse.toBody() = CalibrationBody(
    strategyId = strategyId,
    horizon = horizon,
    origin = origin,
    sampleSize = sampleSize,
    buckets = buckets.map {
        CalibrationBucketBody(it.lower, it.upper, it.sampleSize, it.meanConfidence, it.hitRate, it.wilsonLow, it.wilsonHigh)
    },
)

internal fun EquityResponse.toBody() = EquityBody(
    strategyId = strategyId,
    secid = secid,
    horizon = horizon,
    origin = origin,
    trades = trades,
    points = points.map { EquityPointBody(it.date, it.equity, it.periodReturn, it.trades) },
)

internal fun LeaderboardResponse.toBody() = LeaderboardBody(
    horizon = horizon,
    origin = origin,
    from = from,
    till = till,
    minSampleSize = minSampleSize,
    rows = rows.map {
        LeaderboardRowBody(
            rank = it.rank,
            strategyId = it.strategyId,
            strategyCode = it.strategyCode,
            strategyVersion = it.strategyVersion,
            kind = it.kind,
            isBaseline = it.isBaseline,
            stats = it.stats.toBody(),
            paper = it.paper.toBody(),
            hitRateVsBestBaseline = it.hitRateVsBestBaseline,
        )
    },
    warnings = warnings,
)
