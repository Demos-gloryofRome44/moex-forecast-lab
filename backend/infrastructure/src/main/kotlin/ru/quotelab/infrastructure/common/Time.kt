package ru.quotelab.infrastructure.common

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal fun Instant.toUtc(): OffsetDateTime = atOffset(ZoneOffset.UTC)
