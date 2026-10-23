package ru.quotelab.usecase.common

import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.ForecastStatus
import ru.quotelab.domain.problems.InvalidRequestException

internal fun parseOrigin(raw: String?): ForecastOrigin =
    raw?.let { parseEnum<ForecastOrigin>(it, "origin") } ?: ForecastOrigin.LIVE

internal fun parseStatus(raw: String?): ForecastStatus? = raw?.let { parseEnum<ForecastStatus>(it, "status") }

internal inline fun <reified E : Enum<E>> parseEnum(raw: String, field: String): E =
    enumValues<E>().firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
        ?: throw InvalidRequestException("Invalid value for '$field'", listOf("$field must be one of ${enumValues<E>().map { it.name }}"))
