package ru.quotelab.usecase.common

interface QuotelabMetrics {
    fun candlesSynced(secid: String, count: Int)
    fun syncFailed(secid: String)
    fun forecastsCreated(count: Int)
    fun forecastsEvaluated(count: Int)
    fun <T> timeJob(job: String, block: () -> T): T

    object Noop : QuotelabMetrics {
        override fun candlesSynced(secid: String, count: Int) = Unit
        override fun syncFailed(secid: String) = Unit
        override fun forecastsCreated(count: Int) = Unit
        override fun forecastsEvaluated(count: Int) = Unit
        override fun <T> timeJob(job: String, block: () -> T): T = block()
    }
}
