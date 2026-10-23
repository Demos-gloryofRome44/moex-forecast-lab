package ru.quotelab.usecase.performance

import ru.quotelab.domain.evaluation.EvaluatedForecast
import ru.quotelab.usecase.common.PaperTradingSettings
import ru.quotelab.usecase.performance.dto.EquityPoint
import ru.quotelab.usecase.performance.dto.PaperTradingStats
import java.math.BigDecimal
import java.math.MathContext

internal class PaperTrading(private val settings: PaperTradingSettings) {
    private val mc = MathContext.DECIMAL64
    private val roundTripCost = (settings.commissionPerSide + settings.slippagePerSide).multiply(BigDecimal(2))

    fun netReturn(item: EvaluatedForecast): BigDecimal {
        val position = item.forecast.direction.sign
        if (position == 0) return BigDecimal.ZERO
        return item.evaluation.actualReturn.multiply(BigDecimal(position)) - roundTripCost
    }

    fun equityCurve(items: List<EvaluatedForecast>): List<EquityPoint> {
        var equity = BigDecimal.ONE
        return items.groupBy { it.evaluation.targetTradeDate }.toSortedMap().map { (date, realized) ->
            val periodReturn = realized
                .map { netReturn(it).divide(BigDecimal(it.forecast.horizon.days), mc) }
                .fold(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal(realized.size), mc)
            equity = equity.multiply(BigDecimal.ONE + periodReturn, mc)
            EquityPoint(date, equity, periodReturn, realized.count { it.forecast.direction.sign != 0 })
        }
    }

    fun stats(items: List<EvaluatedForecast>): PaperTradingStats {
        val curve = equityCurve(items)
        return PaperTradingStats(
            trades = items.count { it.forecast.direction.sign != 0 },
            totalReturn = (curve.lastOrNull()?.equity ?: BigDecimal.ONE) - BigDecimal.ONE,
            sharpe = Statistics.sharpe(curve.map { it.periodReturn.toDouble() }, TRADING_DAYS_PER_YEAR),
            maxDrawdown = Statistics.maxDrawdown(listOf(BigDecimal.ONE) + curve.map { it.equity }),
            commissionPerSide = settings.commissionPerSide,
            slippagePerSide = settings.slippagePerSide,
        )
    }

    private companion object {
        const val TRADING_DAYS_PER_YEAR = 252.0
    }
}
