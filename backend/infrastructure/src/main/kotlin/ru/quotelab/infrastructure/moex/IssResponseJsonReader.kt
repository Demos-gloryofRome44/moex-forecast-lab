package ru.quotelab.infrastructure.moex

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.json.common.JsonReader

@Component
class IssResponseJsonReader : JsonReader<IssResponse> {
    override fun read(parser: JsonParser): IssResponse {
        expect(parser.currentToken(), JsonToken.START_OBJECT)
        val blocks = linkedMapOf<String, IssTable>()
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            val name = parser.currentName()
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                blocks[name] = readTable(parser)
            } else {
                parser.skipChildren()
            }
        }
        return IssResponse(blocks)
    }

    private fun readTable(parser: JsonParser): IssTable {
        var columns = emptyList<String>()
        var rows = emptyList<List<Any?>>()
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            val field = parser.currentName()
            parser.nextToken()
            when (field) {
                "columns" -> columns = readArray(parser) { p -> p.text }
                "data" -> rows = readArray(parser) { p -> readArray(p, ::scalar) }
                else -> parser.skipChildren()
            }
        }
        return IssTable(columns, rows)
    }

    private fun <T> readArray(parser: JsonParser, element: (JsonParser) -> T): List<T> {
        expect(parser.currentToken(), JsonToken.START_ARRAY)
        val result = mutableListOf<T>()
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            result += element(parser)
        }
        return result
    }

    private fun scalar(parser: JsonParser): Any? = when (parser.currentToken()) {
        JsonToken.VALUE_NULL -> null
        JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_FLOAT -> parser.decimalValue
        JsonToken.VALUE_TRUE -> true
        JsonToken.VALUE_FALSE -> false
        JsonToken.START_ARRAY, JsonToken.START_OBJECT -> {
            parser.skipChildren()
            null
        }
        else -> parser.text
    }

    private fun expect(actual: JsonToken?, expected: JsonToken) {
        if (actual != expected) throw com.fasterxml.jackson.core.JsonParseException(null, "Expected $expected but was $actual")
    }
}
