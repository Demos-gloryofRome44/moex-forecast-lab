package ru.quotelab.app.seed

import org.slf4j.LoggerFactory
import ru.quotelab.app.config.QuotelabConfig
import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.valueobject.Board
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.usecase.instrument.InstrumentService
import ru.quotelab.usecase.pipeline.PipelineService
import ru.quotelab.usecase.strategy.StrategyService
import ru.tinkoff.kora.application.graph.Lifecycle
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import kotlin.concurrent.thread

@Root
@Component
class StartupInitializer(
    private val config: QuotelabConfig,
    private val strategies: StrategyService,
    private val instruments: InstrumentService,
    private val pipeline: PipelineService,
) : Lifecycle {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun init() {
        strategies.registerCatalog()
        instruments.seed(
            config.watchlist().map { Instrument(Secid.of(it.secid()), Board.of(it.board()), it.name()) },
        )
        val jobs = config.jobs()
        if (jobs.enabled() && jobs.syncOnStartup()) {
            thread(name = "startup-sync", isDaemon = true) {
                try {
                    Thread.sleep(jobs.startupDelay().toMillis())
                    val report = pipeline.syncCandles()
                    log.info("Startup sync finished: {}", report.instruments.map { "${it.secid}=${it.candles}" })
                    pipeline.runForecasts()
                } catch (e: Exception) {
                    log.warn("Startup sync failed, scheduled jobs will retry: {}", e.message)
                }
            }
        }
    }

    override fun release() = Unit
}
