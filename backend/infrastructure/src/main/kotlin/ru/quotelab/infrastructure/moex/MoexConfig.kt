package ru.quotelab.infrastructure.moex

import ru.tinkoff.kora.config.common.annotation.ConfigSource
import java.time.Duration

@ConfigSource("moex")
interface MoexConfig {
    fun minRequestInterval(): Duration

    fun pageSize(): Int
}
