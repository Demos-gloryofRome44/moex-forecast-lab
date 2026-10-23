package ru.quotelab.restapi.common

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class ErrorResponse(
    val error: String,
    val details: List<String> = emptyList(),
)
