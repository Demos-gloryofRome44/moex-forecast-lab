package ru.quotelab.restapi.strategy.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.util.UUID

@Json
data class StrategyBody(
    val id: UUID,
    val code: String,
    val version: String,
    val kind: String,
    val params: Map<String, String>,
    val isActive: Boolean,
    val isBaseline: Boolean,
)
