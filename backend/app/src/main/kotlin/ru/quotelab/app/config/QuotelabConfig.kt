package ru.quotelab.app.config

import ru.tinkoff.kora.config.common.annotation.ConfigSource
import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor
import java.time.Duration

@ConfigSource("quotelab")
interface QuotelabConfig {
    fun watchlist(): List<WatchlistItem>
    fun market(): MarketConfig
    fun forecast(): ForecastConfig
    fun evaluation(): EvaluationConfig
    fun paperTrading(): PaperTradingConfig
    fun metrics(): MetricsConfig
    fun strategies(): StrategiesConfig
    fun jobs(): JobsConfig

    @ConfigValueExtractor
    interface WatchlistItem {
        fun secid(): String
        fun board(): String
        fun name(): String
    }

    @ConfigValueExtractor
    interface MarketConfig {
        fun zone(): String
        fun initialLoadYears(): Long
        fun includeWeekendSessions(): Boolean
    }

    @ConfigValueExtractor
    interface ForecastConfig {
        fun horizons(): List<Int>
        fun maxStaleDays(): Long
    }

    @ConfigValueExtractor
    interface EvaluationConfig {
        fun flatThreshold(): String
        fun batchSize(): Int
    }

    @ConfigValueExtractor
    interface PaperTradingConfig {
        fun commissionPerSide(): String
        fun slippagePerSide(): String
    }

    @ConfigValueExtractor
    interface MetricsConfig {
        fun minSampleSize(): Int
    }

    @ConfigValueExtractor
    interface JobsConfig {
        fun enabled(): Boolean
        fun syncOnStartup(): Boolean
        fun startupDelay(): Duration
    }

    @ConfigValueExtractor
    interface StrategiesConfig {
        fun smaCrossover(): SmaCrossover
        fun rsi(): Rsi
        fun macd(): Macd
        fun bollinger(): Bollinger
        fun baselines(): Baselines
        fun ensemble(): Ensemble
    }

    @ConfigValueExtractor
    interface SmaCrossover {
        fun enabled(): Boolean
        fun version(): String
        fun fast(): Int
        fun slow(): Int
    }

    @ConfigValueExtractor
    interface Rsi {
        fun enabled(): Boolean
        fun version(): String
        fun period(): Int
        fun oversold(): Double
        fun overbought(): Double
    }

    @ConfigValueExtractor
    interface Macd {
        fun enabled(): Boolean
        fun version(): String
        fun fast(): Int
        fun slow(): Int
        fun signal(): Int
    }

    @ConfigValueExtractor
    interface Bollinger {
        fun enabled(): Boolean
        fun version(): String
        fun period(): Int
        fun width(): String
    }

    @ConfigValueExtractor
    interface Baselines {
        fun version(): String
        fun randomSeed(): Long
    }

    @ConfigValueExtractor
    interface Ensemble {
        fun enabled(): Boolean
        fun version(): String
        fun eta(): Double
    }
}
