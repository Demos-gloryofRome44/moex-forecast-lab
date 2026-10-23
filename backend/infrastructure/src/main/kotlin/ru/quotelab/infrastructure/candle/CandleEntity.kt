package ru.quotelab.infrastructure.candle

import ru.tinkoff.kora.database.jdbc.EntityJdbc
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@EntityJdbc
data class CandleEntity(
    val secid: String,
    val interval: Int,
    val beginAt: OffsetDateTime,
    val tradeDate: LocalDate,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long,
)
