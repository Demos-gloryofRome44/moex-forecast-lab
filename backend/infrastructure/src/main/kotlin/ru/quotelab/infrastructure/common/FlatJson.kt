package ru.quotelab.infrastructure.common

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import java.io.StringWriter

internal object FlatJson {
    private val factory = JsonFactory()

    fun write(values: Map<String, String>): String {
        val out = StringWriter()
        factory.createGenerator(out).use { gen ->
            gen.writeStartObject()
            values.toSortedMap().forEach { (k, v) -> gen.writeStringField(k, v) }
            gen.writeEndObject()
        }
        return out.toString()
    }

    fun read(json: String): Map<String, String> {
        val result = linkedMapOf<String, String>()
        factory.createParser(json).use { parser ->
            check(parser.nextToken() == JsonToken.START_OBJECT) { "Expected JSON object" }
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                val name = parser.currentName
                parser.nextToken()
                result[name] = parser.valueAsString ?: parser.text
            }
        }
        return result
    }
}
