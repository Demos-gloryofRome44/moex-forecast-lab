package ru.quotelab.usecase.strategy.dto

import java.util.UUID

data class StrategyResponse(
    val id: UUID,
    val code: String,
    val version: String,
    val kind: String,
    val params: Map<String, String>,
    val isActive: Boolean,
    val isBaseline: Boolean,
)
