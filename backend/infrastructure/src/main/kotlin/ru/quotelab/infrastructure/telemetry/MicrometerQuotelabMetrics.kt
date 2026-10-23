package ru.quotelab.infrastructure.telemetry

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import ru.quotelab.usecase.common.QuotelabMetrics
import ru.tinkoff.kora.common.Component

@Component
class MicrometerQuotelabMetrics(
    private val registry: MeterRegistry,
) : QuotelabMetrics {
    override fun candlesSynced(secid: String, count: Int) {
        registry.counter("quotelab.candles.synced", "secid", secid).increment(count.toDouble())
    }

    override fun syncFailed(secid: String) {
        registry.counter("quotelab.candles.sync.failures", "secid", secid).increment()
    }

    override fun forecastsCreated(count: Int) {
        registry.counter("quotelab.forecasts.created").increment(count.toDouble())
    }

    override fun forecastsEvaluated(count: Int) {
        registry.counter("quotelab.forecasts.evaluated").increment(count.toDouble())
    }

    override fun <T> timeJob(job: String, block: () -> T): T {
        val sample = Timer.start(registry)
        var status = "ok"
        try {
            return block()
        } catch (e: Exception) {
            status = "error"
            throw e
        } finally {
            sample.stop(registry.timer("quotelab.job.duration", "job", job, "status", status))
        }
    }
}
