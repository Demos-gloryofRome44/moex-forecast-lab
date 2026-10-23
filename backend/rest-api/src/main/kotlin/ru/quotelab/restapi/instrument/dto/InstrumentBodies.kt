package ru.quotelab.restapi.instrument.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.validation.common.annotation.NotBlank
import ru.tinkoff.kora.validation.common.annotation.Pattern
import ru.tinkoff.kora.validation.common.annotation.Size
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Json
data class AddInstrumentBody(
    @field:NotBlank
    @field:Size(min = 1, max = 36)
    @field:Pattern("^[A-Za-z0-9_.-]+$")
    val secid: String,
    @field:Pattern("^(TQBR|SNDX)$")
    val board: String? = null,
)

@Json
data class InstrumentBody(
    val secid: String,
    val board: String,
    val name: String,
    val isActive: Boolean,
    val lastTradeDate: LocalDate?,
)

@Json
data class CandleBody(
    val tradeDate: LocalDate,
    val beginAt: Instant,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long,
)
