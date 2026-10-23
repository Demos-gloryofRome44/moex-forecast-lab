package ru.quotelab.domain.common

data class PageRequest(val page: Int, val size: Int) {
    init {
        require(page >= 0) { "page must be >= 0" }
        require(size in 1..MAX_SIZE) { "size must be within 1..$MAX_SIZE" }
    }

    val offset: Long get() = page.toLong() * size

    companion object {
        const val MAX_SIZE = 200
    }
}

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
)
