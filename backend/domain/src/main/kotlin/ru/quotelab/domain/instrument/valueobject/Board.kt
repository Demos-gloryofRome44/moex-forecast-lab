package ru.quotelab.domain.instrument.valueobject

enum class Board(val code: String, val engine: String, val market: String) {
    TQBR("TQBR", "stock", "shares"),
    SNDX("SNDX", "stock", "index"),
    ;

    companion object {
        fun of(code: String): Board =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported board '$code', expected one of ${entries.map { it.code }}")
    }
}
