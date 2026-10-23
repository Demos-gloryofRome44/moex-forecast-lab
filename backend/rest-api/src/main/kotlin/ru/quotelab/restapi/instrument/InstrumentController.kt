package ru.quotelab.restapi.instrument

import ru.quotelab.restapi.instrument.dto.AddInstrumentBody
import ru.quotelab.restapi.instrument.dto.CandleBody
import ru.quotelab.restapi.instrument.dto.InstrumentBody
import ru.quotelab.restapi.instrument.mapper.toBody
import ru.quotelab.restapi.instrument.mapper.toRequest
import ru.quotelab.usecase.candle.CandleQueryService
import ru.quotelab.usecase.candle.dto.CandleQuery
import ru.quotelab.usecase.instrument.InstrumentService
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.validation.common.annotation.Valid
import ru.tinkoff.kora.validation.common.annotation.Validate
import java.time.LocalDate

@Component
@HttpController
open class InstrumentController(
    private val instruments: InstrumentService,
    private val candles: CandleQueryService,
) {
    @HttpRoute(method = HttpMethod.GET, path = "/api/instruments")
    @Json
    fun list(): List<InstrumentBody> = instruments.findAll().map { it.toBody() }

    @HttpRoute(method = HttpMethod.POST, path = "/api/instruments")
    @Json
    @Validate
    open fun add(@Valid @Json body: AddInstrumentBody): HttpResponseEntity<InstrumentBody> =
        HttpResponseEntity.of(CREATED, instruments.add(body.toRequest()).toBody())

    @HttpRoute(method = HttpMethod.GET, path = "/api/instruments/{secid}/candles")
    @Json
    fun candles(
        @Path secid: String,
        @Query from: LocalDate?,
        @Query till: LocalDate?,
        @Query interval: Int?,
    ): List<CandleBody> = candles.find(CandleQuery(secid, from, till, interval)).map { it.toBody() }

    private companion object {
        const val CREATED = 201
    }
}
