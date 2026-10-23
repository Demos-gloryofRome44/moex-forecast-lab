package ru.quotelab.domain.problems

class MarketDataUnavailableException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
