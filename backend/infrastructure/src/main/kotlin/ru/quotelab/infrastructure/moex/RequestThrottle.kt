package ru.quotelab.infrastructure.moex

import java.time.Duration

internal class RequestThrottle(private val minInterval: Duration) {
    private var lastRequestNanos = 0L

    @Synchronized
    fun acquire() {
        val wait = lastRequestNanos + minInterval.toNanos() - System.nanoTime()
        if (wait > 0) Thread.sleep(wait / NANOS_PER_MILLI, (wait % NANOS_PER_MILLI).toInt())
        lastRequestNanos = System.nanoTime()
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
