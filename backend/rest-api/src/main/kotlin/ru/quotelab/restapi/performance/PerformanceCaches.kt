package ru.quotelab.restapi.performance

import ru.quotelab.restapi.performance.dto.CalibrationBody
import ru.quotelab.restapi.performance.dto.EquityBody
import ru.quotelab.restapi.performance.dto.LeaderboardBody
import ru.quotelab.restapi.performance.dto.PerformanceBody
import ru.quotelab.usecase.performance.dto.LeaderboardQuery
import ru.quotelab.usecase.performance.dto.PerformanceQuery
import ru.tinkoff.kora.cache.annotation.Cache
import ru.tinkoff.kora.cache.caffeine.CaffeineCache
import java.util.UUID

@Cache("quotelab.cache.leaderboard")
interface LeaderboardCache : CaffeineCache<LeaderboardQuery, LeaderboardBody>

@Cache("quotelab.cache.performance")
interface PerformanceCache : CaffeineCache<PerformanceQuery, PerformanceBody>

@Cache("quotelab.cache.calibration")
interface CalibrationCache : CaffeineCache<CalibrationKey, CalibrationBody>

@Cache("quotelab.cache.equity")
interface EquityCache : CaffeineCache<EquityKey, EquityBody>

data class CalibrationKey(val strategyId: UUID, val horizon: Int?, val origin: String?)

data class EquityKey(val strategyId: UUID, val secid: String?, val horizon: Int, val origin: String?)
