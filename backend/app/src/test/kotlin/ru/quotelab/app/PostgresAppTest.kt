package ru.quotelab.app

import org.testcontainers.containers.PostgreSQLContainer
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import java.net.ServerSocket

abstract class PostgresAppTest : KoraAppTestConfigModifier {
    override fun config(): KoraConfigModification = KoraConfigModification
        .ofSystemProperty("DB_URL", postgres.jdbcUrl)
        .withSystemProperty("DB_USER", postgres.username)
        .withSystemProperty("DB_PASSWORD", postgres.password)
        .withSystemProperty("HTTP_PORT", freePort().toString())
        .withSystemProperty("PRIVATE_HTTP_PORT", freePort().toString())
        .withSystemProperty("QUOTELAB_JOBS_ENABLED", "false")
        .withSystemProperty("QUOTELAB_SYNC_ON_STARTUP", "false")

    companion object {
        val postgres: PostgreSQLContainer<*> by lazy {
            PostgreSQLContainer("postgres:16-alpine").also { it.start() }
        }

        private fun freePort(): Int = ServerSocket(0).use { it.localPort }
    }
}
