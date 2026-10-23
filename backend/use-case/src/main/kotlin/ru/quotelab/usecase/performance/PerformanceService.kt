package ru.quotelab.usecase.performance

import ru.quotelab.domain.evaluation.EvaluatedForecast
import ru.quotelab.domain.evaluation.Outcome
import ru.quotelab.domain.evaluation.access.EvaluatedFilter
import ru.quotelab.domain.evaluation.access.EvaluationRepository
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.problems.InstrumentNotFoundException
import ru.quotelab.domain.problems.StrategyNotFoundException
import ru.quotelab.domain.strategy.StrategyDescriptor
import ru.quotelab.domain.strategy.StrategyKind
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.usecase.common.MetricsSettings
import ru.quotelab.usecase.common.PaperTradingSettings
import ru.quotelab.usecase.common.parseOrigin
import ru.quotelab.usecase.common.requireValidRange
import ru.quotelab.usecase.performance.dto.CalibrationBucket
import ru.quotelab.usecase.performance.dto.CalibrationResponse
import ru.quotelab.usecase.performance.dto.EquityResponse
import ru.quotelab.usecase.performance.dto.HitRateStats
import ru.quotelab.usecase.performance.dto.LeaderboardQuery
import ru.quotelab.usecase.performance.dto.LeaderboardResponse
import ru.quotelab.usecase.performance.dto.LeaderboardRow
import ru.quotelab.usecase.performance.dto.PerformanceQuery
import ru.quotelab.usecase.performance.dto.PerformanceResponse
import java.util.UUID

class PerformanceService(
    private val strategies: StrategyRepository,
    private val instruments: InstrumentRepository,
    private val evaluations: EvaluationRepository,
    paperSettings: PaperTradingSettings,
    private val settings: MetricsSettings,
) {
    private val paper = PaperTrading(paperSettings)

    fun performance(query: PerformanceQuery): PerformanceResponse {
        requireValidRange(query.from, query.till)
        val strategy = strategy(query.strategyId)
        val secid = query.secid?.let(::instrument)
        val horizon = query.horizon?.let(::Horizon)
        val origin = parseOrigin(query.origin)
        val items = evaluations.findEvaluated(
            EvaluatedFilter(origin, setOf(strategy.id), secid, horizon, query.from, query.till),
        )
        return PerformanceResponse(
            strategyId = strategy.id.value,
            strategyCode = strategy.code,
            strategyVersion = strategy.version,
            kind = strategy.kind.name,
            secid = secid?.value,
            horizon = horizon?.days,
            origin = origin.name,
            stats = hitRateStats(items),
            paper = paper.stats(items),
        )
    }

    fun calibration(strategyId: UUID, horizon: Int?, origin: String?): CalibrationResponse {
        val strategy = strategy(strategyId)
        val parsedOrigin = parseOrigin(origin)
        val decided = evaluations.findEvaluated(
            EvaluatedFilter(parsedOrigin, setOf(strategy.id), horizon = horizon?.let(::Horizon)),
        ).filter { it.evaluation.outcome != Outcome.NEUTRAL }

        val buckets = (0 until BUCKETS).map { i ->
            val lower = i.toDouble() / BUCKETS
            val upper = (i + 1).toDouble() / BUCKETS
            val inBucket = decided.filter {
                val c = it.forecast.confidence.value
                c >= lower && (c < upper || (i == BUCKETS - 1 && c <= upper))
            }
            val hits = inBucket.count { it.evaluation.outcome == Outcome.HIT }
            val interval = Statistics.wilson(hits, inBucket.size, settings.confidenceLevelZ)
            CalibrationBucket(
                lower = lower,
                upper = upper,
                sampleSize = inBucket.size,
                meanConfidence = Statistics.mean(inBucket.map { it.forecast.confidence.value }),
                hitRate = if (inBucket.isEmpty()) null else hits.toDouble() / inBucket.size,
                wilsonLow = interval.low,
                wilsonHigh = interval.high,
            )
        }.filter { it.lower >= 0.5 || it.sampleSize > 0 }
        return CalibrationResponse(strategy.id.value, horizon, parsedOrigin.name, decided.size, buckets)
    }

    fun equity(strategyId: UUID, secid: String?, horizon: Int, origin: String?): EquityResponse {
        val strategy = strategy(strategyId)
        val parsedSecid = secid?.let(::instrument)
        val parsedOrigin = parseOrigin(origin)
        val items = evaluations.findEvaluated(
            EvaluatedFilter(parsedOrigin, setOf(strategy.id), parsedSecid, Horizon(horizon)),
        )
        val curve = paper.equityCurve(items)
        return EquityResponse(strategy.id.value, parsedSecid?.value, horizon, parsedOrigin.name, curve.sumOf { it.trades }, curve)
    }

    fun leaderboard(query: LeaderboardQuery): LeaderboardResponse {
        requireValidRange(query.from, query.till)
        val horizon = Horizon(query.horizon)
        val origin = parseOrigin(query.origin)
        val all = strategies.findAll()
        val items = evaluations.findEvaluated(EvaluatedFilter(origin, horizon = horizon, from = query.from, till = query.till))
            .groupBy { it.forecast.strategyId }

        val unranked = all.filter { it.isActive || items.containsKey(it.id) }.map { descriptor ->
            val own = items[descriptor.id].orEmpty()
            descriptor to Pair(hitRateStats(own), paper.stats(own))
        }
        val bestBaseline = unranked
            .filter { (d, s) -> d.kind == StrategyKind.BASELINE && s.first.hitRate != null }
            .maxOfOrNull { it.second.first.hitRate!! }

        val rows = unranked
            .sortedWith(
                compareBy<Pair<StrategyDescriptor, Pair<HitRateStats, *>>> { it.second.first.insufficientData }
                    .thenByDescending { it.second.first.wilsonLow }
                    .thenBy { it.second.first.meanBrier ?: Double.MAX_VALUE },
            )
            .mapIndexed { index, (descriptor, stats) ->
                LeaderboardRow(
                    rank = index + 1,
                    strategyId = descriptor.id.value,
                    strategyCode = descriptor.code,
                    strategyVersion = descriptor.version,
                    kind = descriptor.kind.name,
                    isBaseline = descriptor.kind == StrategyKind.BASELINE,
                    stats = stats.first,
                    paper = stats.second,
                    hitRateVsBestBaseline = stats.first.hitRate?.let { hr -> bestBaseline?.let { (hr - it) * PERCENT } },
                )
            }
        return LeaderboardResponse(horizon.days, origin.name, query.from, query.till, settings.minSampleSize, rows, warnings(rows.size, origin))
    }

    private fun hitRateStats(items: List<EvaluatedForecast>): HitRateStats {
        val hits = items.count { it.evaluation.outcome == Outcome.HIT }
        val misses = items.count { it.evaluation.outcome == Outcome.MISS }
        val n = hits + misses
        val interval = Statistics.wilson(hits, n, settings.confidenceLevelZ)
        val enough = n >= settings.minSampleSize
        return HitRateStats(
            evaluated = items.size,
            sampleSize = n,
            hits = hits,
            misses = misses,
            neutrals = items.size - n,
            hitRate = if (n == 0) null else hits.toDouble() / n,
            wilsonLow = interval.low,
            wilsonHigh = interval.high,
            meanBrier = Statistics.mean(items.mapNotNull { it.evaluation.brier?.toDouble() }),
            insufficientData = !enough,
            beatsCoinFlip = enough && interval.low > COIN_FLIP,
        )
    }

    private fun warnings(strategiesCompared: Int, origin: ForecastOrigin): List<String> = buildList {
        add(
            "Множественные сравнения: сравнивается $strategiesCompared стратегий; при 95%-интервалах примерно одна из 20 " +
                "может выглядеть лучше случайной просто по везению. Подтверждайте победителей на отдельном holdout-периоде.",
        )
        add("Вывод «лучше случайной» допустим только при нижней границе интервала Уилсона выше 50% и выборке от ${settings.minSampleSize} оценок.")
        if (origin == ForecastOrigin.BACKTEST) {
            add("Бэктест: прогнозы построены задним числом на исторических данных. Это не живой журнал, результаты нельзя смешивать.")
        }
    }

    private fun strategy(id: UUID): StrategyDescriptor =
        strategies.findById(StrategyId(id)) ?: throw StrategyNotFoundException(id.toString())

    private fun instrument(raw: String): Secid {
        val secid = Secid.of(raw)
        instruments.findBySecid(secid) ?: throw InstrumentNotFoundException(secid.value)
        return secid
    }

    private companion object {
        const val BUCKETS = 10
        const val COIN_FLIP = 0.5
        const val PERCENT = 100.0
    }
}
