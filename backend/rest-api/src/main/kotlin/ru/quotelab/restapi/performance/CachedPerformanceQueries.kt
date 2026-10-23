package ru.quotelab.restapi.performance

import ru.quotelab.restapi.performance.dto.CalibrationBody
import ru.quotelab.restapi.performance.dto.EquityBody
import ru.quotelab.restapi.performance.dto.LeaderboardBody
import ru.quotelab.restapi.performance.dto.PerformanceBody
import ru.quotelab.restapi.performance.mapper.toBody
import ru.quotelab.usecase.performance.PerformanceService
import ru.quotelab.usecase.performance.dto.LeaderboardQuery
import ru.quotelab.usecase.performance.dto.PerformanceQuery
import ru.tinkoff.kora.cache.annotation.CacheInvalidate
import ru.tinkoff.kora.cache.annotation.Cacheable
import ru.tinkoff.kora.common.Component

@Component
open class CachedPerformanceQueries(
    private val service: PerformanceService,
) {
    @Cacheable(LeaderboardCache::class)
    open fun leaderboard(query: LeaderboardQuery): LeaderboardBody = service.leaderboard(query).toBody()

    @Cacheable(PerformanceCache::class)
    open fun performance(query: PerformanceQuery): PerformanceBody = service.performance(query).toBody()

    @Cacheable(CalibrationCache::class)
    open fun calibration(key: CalibrationKey): CalibrationBody = service.calibration(key.strategyId, key.horizon, key.origin).toBody()

    @Cacheable(EquityCache::class)
    open fun equity(key: EquityKey): EquityBody = service.equity(key.strategyId, key.secid, key.horizon, key.origin).toBody()

    @CacheInvalidate(LeaderboardCache::class, invalidateAll = true)
    @CacheInvalidate(PerformanceCache::class, invalidateAll = true)
    @CacheInvalidate(CalibrationCache::class, invalidateAll = true)
    @CacheInvalidate(EquityCache::class, invalidateAll = true)
    open fun invalidateAll() = Unit
}
