package ru.quotelab.domain.strategy

enum class Direction(val sign: Int) {
    UP(1),
    DOWN(-1),
    FLAT(0),
    ;

    companion object {
        fun ofSign(sign: Int): Direction = when {
            sign > 0 -> UP
            sign < 0 -> DOWN
            else -> FLAT
        }
    }
}
