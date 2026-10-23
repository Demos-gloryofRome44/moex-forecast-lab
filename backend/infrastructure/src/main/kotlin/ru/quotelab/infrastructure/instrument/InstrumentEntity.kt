package ru.quotelab.infrastructure.instrument

import ru.tinkoff.kora.database.jdbc.EntityJdbc
@EntityJdbc
data class InstrumentEntity(
    val secid: String,
    val board: String,
    val name: String,
    val isActive: Boolean,
)
