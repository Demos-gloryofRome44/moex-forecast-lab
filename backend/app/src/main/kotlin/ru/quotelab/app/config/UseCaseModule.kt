package ru.quotelab.app.config

import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.evaluation.access.EvaluationRepository
import ru.quotelab.domain.forecast.access.ForecastRepository
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.access.MarketDataProvider
import ru.quotelab.domain.strategy.Strategy
import ru.quotelab.domain.strategy.access.EnsembleWeightRepository
import ru.quotelab.domain.strategy.access.StrategyRepository
import ru.quotelab.domain.strategy.baseline.AlwaysUpBaseline
import ru.quotelab.domain.strategy.baseline.LastDirectionBaseline
import ru.quotelab.domain.strategy.baseline.RandomBaseline
import ru.quotelab.domain.strategy.ensemble.MultiplicativeWeightsEnsemble
import ru.quotelab.domain.strategy.indicatorbased.BollingerStrategy
import ru.quotelab.domain.strategy.indicatorbased.MacdStrategy
import ru.quotelab.domain.strategy.indicatorbased.RsiStrategy
import ru.quotelab.domain.strategy.indicatorbased.SmaCrossoverStrategy
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.usecase.backtest.BacktestService
import ru.quotelab.usecase.candle.CandleQueryService
import ru.quotelab.usecase.candle.CandleSyncService
import ru.quotelab.usecase.common.EvaluationSettings
import ru.quotelab.usecase.common.EventPublisher
import ru.quotelab.usecase.common.ForecastSettings
import ru.quotelab.usecase.common.JobLock
import ru.quotelab.usecase.common.MarketSettings
import ru.quotelab.usecase.common.MetricsSettings
import ru.quotelab.usecase.common.PaperTradingSettings
import ru.quotelab.usecase.common.QuotelabMetrics
import ru.quotelab.usecase.common.TransactionRunner
import ru.quotelab.usecase.ensemble.EnsembleWeightsService
import ru.quotelab.usecase.evaluation.EvaluationService
import ru.quotelab.usecase.forecast.ForecastEngine
import ru.quotelab.usecase.forecast.ForecastQueryService
import ru.quotelab.usecase.forecast.ForecastRunService
import ru.quotelab.usecase.instrument.InstrumentService
import ru.quotelab.usecase.performance.PerformanceService
import ru.quotelab.usecase.pipeline.PipelineService
import ru.quotelab.usecase.strategy.StrategyCatalog
import ru.quotelab.usecase.strategy.StrategyService
import ru.tinkoff.kora.common.Module
import java.math.BigDecimal
import java.time.Clock
import java.time.ZoneId

@Module
interface UseCaseModule {
    fun clock(): Clock = Clock.systemUTC()

    fun jobLock(): JobLock = JobLock()

    fun marketSettings(config: QuotelabConfig): MarketSettings = with(config.market()) {
        MarketSettings(ZoneId.of(zone()), initialLoadYears(), includeWeekendSessions())
    }

    fun forecastSettings(config: QuotelabConfig): ForecastSettings = with(config.forecast()) {
        ForecastSettings(horizons().map(::Horizon), maxStaleDays())
    }

    fun evaluationSettings(config: QuotelabConfig): EvaluationSettings = with(config.evaluation()) {
        EvaluationSettings(BigDecimal(flatThreshold()), batchSize())
    }

    fun paperTradingSettings(config: QuotelabConfig): PaperTradingSettings = with(config.paperTrading()) {
        PaperTradingSettings(BigDecimal(commissionPerSide()), BigDecimal(slippagePerSide()))
    }

    fun metricsSettings(config: QuotelabConfig): MetricsSettings = MetricsSettings(minSampleSize = config.metrics().minSampleSize())

    fun strategyCatalog(config: QuotelabConfig): StrategyCatalog {
        val s = config.strategies()
        val strategies = buildList<Strategy> {
            with(s.baselines()) {
                add(AlwaysUpBaseline(version()))
                add(LastDirectionBaseline(version()))
                add(RandomBaseline(version(), randomSeed()))
            }
            s.smaCrossover().takeIf { it.enabled() }?.let { add(SmaCrossoverStrategy(it.version(), it.fast(), it.slow())) }
            s.rsi().takeIf { it.enabled() }?.let { add(RsiStrategy(it.version(), it.period(), it.oversold(), it.overbought())) }
            s.macd().takeIf { it.enabled() }?.let { add(MacdStrategy(it.version(), it.fast(), it.slow(), it.signal())) }
            s.bollinger().takeIf { it.enabled() }?.let { add(BollingerStrategy(it.version(), it.period(), BigDecimal(it.width()))) }
        }
        val ensemble = s.ensemble().takeIf { it.enabled() }?.let { MultiplicativeWeightsEnsemble(it.version(), it.eta()) }
        return StrategyCatalog(strategies, ensemble)
    }

    fun instrumentService(
        instruments: InstrumentRepository,
        candles: CandleRepository,
        marketData: MarketDataProvider,
    ): InstrumentService = InstrumentService(instruments, candles, marketData)

    fun candleQueryService(instruments: InstrumentRepository, candles: CandleRepository, clock: Clock): CandleQueryService =
        CandleQueryService(instruments, candles, clock)

    fun candleSyncService(
        instruments: InstrumentRepository,
        candles: CandleRepository,
        marketData: MarketDataProvider,
        events: EventPublisher,
        metrics: QuotelabMetrics,
        settings: MarketSettings,
        clock: Clock,
    ): CandleSyncService = CandleSyncService(instruments, candles, marketData, events, metrics, settings, clock)

    fun strategyService(repository: StrategyRepository, catalog: StrategyCatalog): StrategyService =
        StrategyService(repository, catalog)

    fun ensembleWeightsService(
        catalog: StrategyCatalog,
        weights: EnsembleWeightRepository,
        evaluations: EvaluationRepository,
        settings: ForecastSettings,
        evaluationSettings: EvaluationSettings,
    ): EnsembleWeightsService = EnsembleWeightsService(catalog, weights, evaluations, settings, evaluationSettings)

    fun forecastEngine(
        catalog: StrategyCatalog,
        forecasts: ForecastRepository,
        ensembleWeights: EnsembleWeightsService,
        settings: ForecastSettings,
    ): ForecastEngine = ForecastEngine(catalog, forecasts, ensembleWeights, settings)

    fun forecastRunService(
        instruments: InstrumentRepository,
        candles: CandleRepository,
        strategies: StrategyRepository,
        catalog: StrategyCatalog,
        engine: ForecastEngine,
        events: EventPublisher,
        metrics: QuotelabMetrics,
        marketSettings: MarketSettings,
        settings: ForecastSettings,
        clock: Clock,
    ): ForecastRunService =
        ForecastRunService(instruments, candles, strategies, catalog, engine, events, metrics, marketSettings, settings, clock)

    fun evaluationService(
        forecasts: ForecastRepository,
        evaluations: EvaluationRepository,
        candles: CandleRepository,
        strategies: StrategyRepository,
        tx: TransactionRunner,
        events: EventPublisher,
        metrics: QuotelabMetrics,
        marketSettings: MarketSettings,
        settings: EvaluationSettings,
        clock: Clock,
    ): EvaluationService =
        EvaluationService(forecasts, evaluations, candles, strategies, tx, events, metrics, marketSettings, settings, clock)

    fun backtestService(
        instruments: InstrumentRepository,
        candles: CandleRepository,
        catalog: StrategyCatalog,
        engine: ForecastEngine,
        evaluation: EvaluationService,
        metrics: QuotelabMetrics,
        marketSettings: MarketSettings,
        clock: Clock,
    ): BacktestService = BacktestService(instruments, candles, catalog, engine, evaluation, metrics, marketSettings, clock)

    fun pipelineService(
        lock: JobLock,
        sync: CandleSyncService,
        forecastRun: ForecastRunService,
        evaluation: EvaluationService,
        ensembleWeights: EnsembleWeightsService,
        backtest: BacktestService,
        clock: Clock,
    ): PipelineService = PipelineService(lock, sync, forecastRun, evaluation, ensembleWeights, backtest, clock)

    fun forecastQueryService(
        forecasts: ForecastRepository,
        evaluations: EvaluationRepository,
        strategies: StrategyRepository,
    ): ForecastQueryService = ForecastQueryService(forecasts, evaluations, strategies)

    fun performanceService(
        strategies: StrategyRepository,
        instruments: InstrumentRepository,
        evaluations: EvaluationRepository,
        paperSettings: PaperTradingSettings,
        settings: MetricsSettings,
    ): PerformanceService = PerformanceService(strategies, instruments, evaluations, paperSettings, settings)
}
