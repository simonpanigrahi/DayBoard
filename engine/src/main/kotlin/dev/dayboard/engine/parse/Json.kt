package dev.dayboard.engine.parse

/**
 * A small, forgiving JSON reader. The engine takes no dependencies, and what arrives
 * here is model output rather than a wire format: fences and chatter around the object
 * are ignored and trailing commas are tolerated.
 */
internal sealed interface JsonValue {
    data class Obj(val fields: Map<String, JsonValue>) : JsonValue
    data class Arr(val items: List<JsonValue>) : JsonValue
    data class Str(val value: String) : JsonValue
    data class Num(val value: Double) : JsonValue
    data class Bool(val value: Boolean) : JsonValue
    data object Null : JsonValue
}

private data class Parsed<out T>(val value: T, val next: Int)

internal fun parseJson(text: String): JsonValue? {
    val start = text.indexOf('{')
    val end = text.lastIndexOf('}')
    if (start < 0 || end <= start) return null
    return readValue(text.substring(start, end + 1), 0)?.value
}

private fun readValue(text: String, from: Int): Parsed<JsonValue>? {
    val at = skipSpace(text, from)
    if (at >= text.length) return null
    return when (text[at]) {
        '{' -> readFields(text, at + 1, emptyMap())?.let { Parsed(JsonValue.Obj(it.value), it.next) }
        '[' -> readItems(text, at + 1, emptyList())?.let { Parsed(JsonValue.Arr(it.value), it.next) }
        '"' -> readString(text, at)?.let { Parsed(JsonValue.Str(it.value), it.next) }
        't' -> readLiteral(text, at, "true")?.let { Parsed(JsonValue.Bool(true), it) }
        'f' -> readLiteral(text, at, "false")?.let { Parsed(JsonValue.Bool(false), it) }
        'n' -> readLiteral(text, at, "null")?.let { Parsed(JsonValue.Null, it) }
        else -> readNumber(text, at)
    }
}

private tailrec fun readFields(
    text: String,
    from: Int,
    fields: Map<String, JsonValue>
): Parsed<Map<String, JsonValue>>? {
    val at = skipSpace(text, from)
    if (at >= text.length) return null
    if (text[at] == '}') return Parsed(fields, at + 1)

    val key = readString(text, at) ?: return null
    val colon = skipSpace(text, key.next)
    if (colon >= text.length || text[colon] != ':') return null
    val value = readValue(text, colon + 1) ?: return null
    val after = skipSpace(text, value.next)
    val next = if (after < text.length && text[after] == ',') after + 1 else after
    return readFields(text, next, fields + (key.value to value.value))
}

private tailrec fun readItems(text: String, from: Int, items: List<JsonValue>): Parsed<List<JsonValue>>? {
    val at = skipSpace(text, from)
    if (at >= text.length) return null
    if (text[at] == ']') return Parsed(items, at + 1)

    val value = readValue(text, at) ?: return null
    val after = skipSpace(text, value.next)
    val next = if (after < text.length && text[after] == ',') after + 1 else after
    return readItems(text, next, items + value.value)
}

private fun readString(text: String, from: Int): Parsed<String>? {
    val at = skipSpace(text, from)
    if (at >= text.length || text[at] != '"') return null
    return readChars(text, at + 1, "")
}

private tailrec fun readChars(text: String, from: Int, acc: String): Parsed<String>? {
    if (from >= text.length) return null
    val char = text[from]
    if (char == '"') return Parsed(acc, from + 1)
    if (char != '\\') return readChars(text, from + 1, acc + char)

    if (from + 1 >= text.length) return null
    return when (val escape = text[from + 1]) {
        'n' -> readChars(text, from + 2, acc + "\n")
        't' -> readChars(text, from + 2, acc + "\t")
        'r' -> readChars(text, from + 2, acc + "\r")
        'u' -> {
            if (from + 6 > text.length) return null
            val code = text.substring(from + 2, from + 6).toIntOrNull(16) ?: return null
            readChars(text, from + 6, acc + code.toChar())
        }
        else -> readChars(text, from + 2, acc + escape)
    }
}

private fun readNumber(text: String, from: Int): Parsed<JsonValue>? {
    val end = (from until text.length).firstOrNull { text[it] !in "+-.eE0123456789" } ?: text.length
    val number = text.substring(from, end).toDoubleOrNull() ?: return null
    return Parsed(JsonValue.Num(number), end)
}

private fun readLiteral(text: String, from: Int, word: String): Int? =
    if (text.startsWith(word, from)) from + word.length else null

private tailrec fun skipSpace(text: String, from: Int): Int =
    if (from < text.length && text[from].isWhitespace()) skipSpace(text, from + 1) else from
