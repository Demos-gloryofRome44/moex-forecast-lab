package ru.quotelab.domain.forecast

import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.domain.strategy.valueobject.StrategyId
import java.time.Instant
import java.time.LocalDate

data class Forecast(
    val id: ForecastId,
    val strategyId: StrategyId,
    val secid: Secid,
    val horizon: Horizon,
    val issuedAt: Instant,
    val tradeDate: LocalDate,
    val basePrice: Price,
    val direction: Direction,
    val confidence: Confidence,
    val expectedTargetDate: LocalDate,
    val status: ForecastStatus,
    val origin: ForecastOrigin,
) {
    companion object {
        fun issue(
            strategyId: StrategyId,
            secid: Secid,
            horizon: Horizon,
            tradeDate: LocalDate,
            basePrice: Price,
            signal: Signal,
            issuedAt: Instant,
            origin: ForecastOrigin,
        ) = Forecast(
            id = ForecastId(),
            strategyId = strategyId,
            secid = secid,
            horizon = horizon,
            issuedAt = issuedAt,
            tradeDate = tradeDate,
            basePrice = basePrice,
            direction = signal.direction,
            confidence = signal.confidence,
            expectedTargetDate = TradingCalendar.addWeekdays(tradeDate, horizon.days),
            status = ForecastStatus.PENDING,
            origin = origin,
        )
    }
}
