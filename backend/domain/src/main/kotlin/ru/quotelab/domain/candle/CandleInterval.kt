package ru.quotelab.domain.candle

enum class CandleInterval(val issCode: Int) {
    DAY(24),
    ;

    companion object {
        fun ofIssCode(code: Int): CandleInterval =
            entries.firstOrNull { it.issCode == code }
                ?: throw IllegalArgumentException("Unsupported interval $code, expected one of ${entries.map { it.issCode }}")
    }
}
