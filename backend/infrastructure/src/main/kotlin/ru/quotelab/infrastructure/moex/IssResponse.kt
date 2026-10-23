package ru.quotelab.infrastructure.moex

data class IssResponse(val blocks: Map<String, IssTable>) {
    fun table(name: String): IssTable = blocks[name] ?: IssTable(emptyList(), emptyList())
}

data class IssTable(val columns: List<String>, val rows: List<List<Any?>>) {
    private val index: Map<String, Int> = columns.withIndex().associate { (i, c) -> c.lowercase() to i }

    fun records(): List<IssRecord> = rows.map { IssRecord(index, it) }
}

class IssRecord(private val index: Map<String, Int>, private val values: List<Any?>) {
    operator fun get(column: String): Any? = index[column.lowercase()]?.let { values.getOrNull(it) }

    fun string(column: String): String? = get(column)?.toString()

    fun decimal(column: String): java.math.BigDecimal? = when (val v = get(column)) {
        null -> null
        is java.math.BigDecimal -> v
        else -> v.toString().toBigDecimalOrNull()
    }
}
