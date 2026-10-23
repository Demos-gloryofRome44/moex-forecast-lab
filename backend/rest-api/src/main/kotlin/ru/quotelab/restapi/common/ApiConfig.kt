package ru.quotelab.restapi.common

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("quotelab.api")
interface ApiConfig {
    fun adminEnabled(): Boolean

    fun maxPageSize(): Int
}
