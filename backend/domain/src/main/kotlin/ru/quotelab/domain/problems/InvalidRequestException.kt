package ru.quotelab.domain.problems

class InvalidRequestException(message: String, val details: List<String> = emptyList()) :
    RuntimeException(message)
