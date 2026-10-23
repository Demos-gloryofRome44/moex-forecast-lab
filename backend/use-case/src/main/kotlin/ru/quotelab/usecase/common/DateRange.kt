package ru.quotelab.usecase.common

import ru.quotelab.domain.problems.InvalidRequestException
import java.time.LocalDate

internal fun requireValidRange(from: LocalDate?, till: LocalDate?) {
    if (from != null && till != null && from.isAfter(till)) {
        throw InvalidRequestException("Invalid date range", listOf("from ($from) must not be after till ($till)"))
    }
}
