package ru.quotelab.infrastructure.common

import ru.quotelab.usecase.common.TransactionRunner
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.database.jdbc.JdbcConnectionFactory
import ru.tinkoff.kora.database.jdbc.JdbcHelper
import java.sql.Connection

@Component
class JdbcTransactionRunner(
    private val connectionFactory: JdbcConnectionFactory,
) : TransactionRunner {
    override fun <T> inTransaction(block: () -> T): T =
        connectionFactory.inTx(JdbcHelper.SqlFunction1<Connection, T> { block() })
}
