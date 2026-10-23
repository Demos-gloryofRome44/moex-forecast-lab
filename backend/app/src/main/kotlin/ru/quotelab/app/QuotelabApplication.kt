package ru.quotelab.app

import ru.quotelab.app.config.UseCaseModule
import ru.quotelab.infrastructure.InfrastructureModule
import ru.quotelab.restapi.RestApiModule
import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.yaml.YamlConfigModule
import ru.tinkoff.kora.database.flyway.FlywayJdbcDatabaseModule
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule
import ru.tinkoff.kora.http.client.jdk.JdkHttpClientModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.micrometer.module.MetricsModule
import ru.tinkoff.kora.resilient.ResilientModule
import ru.tinkoff.kora.scheduling.quartz.QuartzModule
import ru.tinkoff.kora.validation.module.ValidationModule
import java.util.TimeZone

@KoraApp
interface QuotelabApplication :
    YamlConfigModule,
    LogbackModule,
    JsonModule,
    ValidationModule,
    UndertowHttpServerModule,
    JdkHttpClientModule,
    JdbcDatabaseModule,
    FlywayJdbcDatabaseModule,
    QuartzModule,
    ResilientModule,
    CaffeineCacheModule,
    MetricsModule,
    InfrastructureModule,
    RestApiModule,
    UseCaseModule

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"))
    KoraApplication.run { QuotelabApplicationGraph.graph() }
}
