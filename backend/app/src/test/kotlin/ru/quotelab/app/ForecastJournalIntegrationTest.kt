package ru.quotelab.app

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import ru.quotelab.domain.candle.CandleInterval
import ru.quotelab.domain.candle.access.CandleRepository
import ru.quotelab.domain.candle.valueobject.Price
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.ForecastStatus
import ru.quotelab.domain.forecast.access.ForecastRepository
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.valueobject.Board
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.domain.strategy.Direction
import ru.quotelab.domain.strategy.Signal
import ru.quotelab.domain.strategy.baseline.AlwaysUpBaseline
import ru.quotelab.domain.strategy.valueobject.Confidence
import ru.quotelab.domain.strategy.valueobject.Horizon
import ru.quotelab.usecase.backtest.BacktestService
import ru.quotelab.usecase.backtest.dto.BacktestRequest
import ru.quotelab.usecase.performance.PerformanceService
import ru.quotelab.usecase.performance.dto.LeaderboardQuery
import ru.quotelab.usecase.strategy.StrategyService
import ru.tinkoff.kora.database.jdbc.JdbcDatabase
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Testcontainers(disabledWithoutDocker = true)
@KoraAppTest(QuotelabApplication::class)
class ForecastJournalIntegrationTest : PostgresAppTest() {
    @TestComponent
    lateinit var forecasts: ForecastRepository

    @TestComponent
    lateinit var candles: CandleRepository

    @TestComponent
    lateinit var db: JdbcDatabase

    @TestComponent
    lateinit var backtest: BacktestService

    @TestComponent
    lateinit var performance: PerformanceService

    @TestComponent
    lateinit var instruments: InstrumentRepository

    @TestComponent
    lateinit var strategies: StrategyService

    private val sber = Secid("SBER")

    @BeforeEach
    fun seed() {
        strategies.registerCatalog()
        instruments.save(Instrument(sber, Board.TQBR, "Сбербанк России"))
    }

    private fun issue(tradeDate: LocalDate) = Forecast.issue(
        strategyId = AlwaysUpBaseline("1").id,
        secid = sber,
        horizon = Horizon(1),
        tradeDate = tradeDate,
        basePrice = Price(BigDecimal("100")),
        signal = Signal(Direction.UP, Confidence(0.5)),
        issuedAt = Instant.now(),
        origin = ForecastOrigin.LIVE,
    )

    private fun execute(sql: String, vararg args: Any): Result<Int> = runCatching {
        db.inTx<Int> { connection ->
            connection.prepareStatement(sql).use { ps ->
                args.forEachIndexed { i, arg -> ps.setObject(i + 1, arg) }
                ps.executeUpdate()
            }
        }
    }

    @Test
    fun `forecast is immutable at the database level, only PENDING to EVALUATED is allowed`() {
        val forecast = issue(LocalDate.of(2020, 1, 10))
        assertTrue(forecasts.insertIfAbsent(forecast))

        val edit = execute("UPDATE forecasts SET base_price = 1 WHERE id = ?", forecast.id.value)
        assertTrue(edit.isFailure)
        assertTrue(edit.exceptionOrNull()!!.causeChain().any { it.message.orEmpty().contains("immutable") })

        val delete = execute("DELETE FROM forecasts WHERE id = ?", forecast.id.value)
        assertTrue(delete.isFailure)

        forecasts.markEvaluated(forecast.id)
        assertEquals(ForecastStatus.EVALUATED, forecasts.findById(forecast.id)!!.status)

        val back = execute("UPDATE forecasts SET status = 'PENDING' WHERE id = ?", forecast.id.value)
        assertTrue(back.isFailure)
        assertEquals(0, forecast.basePrice.compareTo(forecasts.findById(forecast.id)!!.basePrice))
    }

    @Test
    fun `the same forecast cannot be issued twice for a trading day`() {
        val date = LocalDate.of(2020, 2, 3)
        assertTrue(forecasts.insertIfAbsent(issue(date)))
        assertFalse(forecasts.insertIfAbsent(issue(date)))
    }

    @Test
    fun `candle upsert is idempotent`() {
        val history = syntheticCandles(LocalDate.of(2019, 1, 1), 10)
        candles.upsertAll(history)
        candles.upsertAll(history)
        val stored = candles.findRange(sber, CandleInterval.DAY, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1))
        assertEquals(10, stored.size)
    }

    @Test
    fun `backtest issues and evaluates forecasts walk-forward and is safe to rerun`() {
        val start = LocalDate.of(2021, 1, 4)
        candles.upsertAll(syntheticCandles(start, 160))
        val request = BacktestRequest(from = start.plusDays(100), till = start.plusDays(220))

        val first = backtest.run(request)
        assertTrue(first.created > 0, "backtest should issue forecasts")
        assertTrue(first.evaluated > 0, "backtest should evaluate forecasts whose target candle exists")

        val second = backtest.run(request)
        assertEquals(0, second.created, "rerun must not duplicate forecasts")

        val leaderboard = performance.leaderboard(LeaderboardQuery(1, null, null, "BACKTEST"))
        assertTrue(leaderboard.rows.any { it.isBaseline && it.stats.sampleSize > 0 })
        assertTrue(leaderboard.rows.all { it.stats.sampleSize <= it.stats.evaluated })
    }

    private fun syntheticCandles(from: LocalDate, weekdays: Int) =
        generateSequence(from) { it.plusDays(1) }
            .filter { it.dayOfWeek.value <= 5 }
            .take(weekdays)
            .mapIndexed { i, date ->
                val close = Price(BigDecimal(100 + i * 0.1 + 5 * kotlin.math.sin(i / 5.0)).setScale(2, java.math.RoundingMode.HALF_UP))
                ru.quotelab.domain.candle.Candle(
                    secid = sber,
                    interval = CandleInterval.DAY,
                    beginAt = date.atStartOfDay().toInstant(ZoneOffset.ofHours(3)),
                    tradeDate = date,
                    open = close,
                    high = close,
                    low = close,
                    close = close,
                    volume = 1_000,
                )
            }.toList()

    private fun Throwable.causeChain(): Sequence<Throwable> = generateSequence(this) { it.cause }
}
