package ru.quotelab.restapi.instrument.mapper

import ru.quotelab.restapi.instrument.dto.AddInstrumentBody
import ru.quotelab.restapi.instrument.dto.CandleBody
import ru.quotelab.restapi.instrument.dto.InstrumentBody
import ru.quotelab.usecase.candle.dto.CandleResponse
import ru.quotelab.usecase.instrument.dto.AddInstrumentRequest
import ru.quotelab.usecase.instrument.dto.InstrumentResponse

internal fun AddInstrumentBody.toRequest() = AddInstrumentRequest(secid = secid, board = board)

internal fun InstrumentResponse.toBody() = InstrumentBody(
    secid = secid,
    board = board,
    name = name,
    isActive = isActive,
    lastTradeDate = lastTradeDate,
)

internal fun CandleResponse.toBody() = CandleBody(
    tradeDate = tradeDate,
    beginAt = beginAt,
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume,
)
