package ru.quotelab.domain.instrument.valueobject

@JvmInline
value class Secid(val value: String) {
    init {
        require(PATTERN.matches(value)) { "Invalid SECID '$value'" }
    }

    override fun toString(): String = value

    companion object {
        private val PATTERN = Regex("^[A-Z0-9_.-]{1,36}$")

        fun of(raw: String): Secid = Secid(raw.trim().uppercase())
    }
}
