package ru.quotelab.usecase.forecast

import org.slf4j.LoggerFactory
import ru.quotelab.domain.candle.Candle
import ru.quotelab.domain.candle.CandleSeries
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.access.ForecastRepository
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.StrategyResult
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import ru.quotelab.usecase.common.ForecastSettings
import ru.quotelab.usecase.ensemble.EnsembleWeightsService
import ru.quotelab.usecase.strategy.StrategyCatalog
import java.time.Instant
import java.time.LocalDate

class ForecastEngine(
    private val catalog: StrategyCatalog,
    private val forecasts: ForecastRepository,
    private val ensembleWeights: EnsembleWeightsService,
    private val settings: ForecastSettings,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    data class IssueResult(val created: List<Forecast>, val insufficientData: Int)

    fun issue(
        instrument: Instrument,
        tradeDate: LocalDate,
        history: List<Candle>,
        origin: ForecastOrigin,
        issuedAt: Instant,
    ): IssueResult {
        val series = CandleSeries.asOf(instrument.secid, tradeDate, history)
        if (series.isEmpty() || series.last.tradeDate != tradeDate) {
            log.debug("No candle for {} on {}, skipping", instrument.secid, tradeDate)
            return IssueResult(emptyList(), 0)
        }
        val basePrice = series.last.close
        val created = mutableListOf<Forecast>()
        var insufficient = 0

        for (horizon in settings.horizons) {
            val memberSignals = mutableMapOf<StrategyId, Signal>()
            for (strategy in catalog.strategies) {
                when (val result = strategy.signal(series, horizon)) {
                    is StrategyResult.InsufficientData -> insufficient++
                    is StrategyResult.Success -> {
                        memberSignals[strategy.id] = result.signal
                        record(Forecast.issue(strategy.id, instrument.secid, horizon, tradeDate, basePrice, result.signal, issuedAt, origin))
                            ?.let(created::add)
                    }
                }
            }
            issueEnsemble(instrument, tradeDate, horizon, memberSignals, origin, issuedAt, basePrice)?.let(created::add)
        }
        return IssueResult(created, insufficient)
    }

    private fun issueEnsemble(
        instrument: Instrument,
        tradeDate: LocalDate,
        horizon: Horizon,
        memberSignals: Map<StrategyId, Signal>,
        origin: ForecastOrigin,
        issuedAt: Instant,
        basePrice: Price,
    ): Forecast? {
        val ensemble = catalog.ensemble ?: return null
        val members = catalog.ensembleMembers.toSet()
        val signals = memberSignals.filterKeys { it in members }
        if (signals.isEmpty()) return null
        val weights = ensembleWeights.weightsAsOf(origin, horizon, issuedAt)
        val signal = ensemble.combine(signals, weights)
        return record(Forecast.issue(ensemble.id, instrument.secid, horizon, tradeDate, basePrice, signal, issuedAt, origin))
    }

    private fun record(forecast: Forecast): Forecast? =
        if (forecasts.insertIfAbsent(forecast)) forecast else null
}
