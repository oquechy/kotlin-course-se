package ru.hse.spb

import java.io.InputStream

enum class Token {
    BEGIN_TABLE,
    END_TABLE,
    CELL
}

fun countCells(tokens: List<Token>, start: Int, counts: MutableList<Int>): Int {
    var count = 0
    var i = start
    while (i < tokens.size) {
        when (tokens[i]) {
            Token.CELL -> {
                count++
                i++
            }
            Token.BEGIN_TABLE -> {
                i = countCells(tokens, i + 1, counts)
            }
            Token.END_TABLE -> {
                counts.add(count)
                return i + 1
            }
        }
    }
    throw IllegalArgumentException("missing </table>")
}

fun tokenize(source: String): List<Token> {
    val split = source.split("><", "<", ">")
    return split.filter { it.isNotBlank() && it != "tr" && it != "/tr" && it != "/td" }.map {
        when (it) {
            "table" -> Token.BEGIN_TABLE
            "/table" -> Token.END_TABLE
            "td" -> Token.CELL
            else -> throw IllegalArgumentException("Unrecognized tag: $it")
        }
    }
}

fun readInput(inputStream: InputStream): String {
    return inputStream.bufferedReader().useLines { lines ->
        val stringBuilder = StringBuilder()
        lines.forEach { stringBuilder.append(it) }
        stringBuilder.toString()
    }
}

fun main(args: Array<String>) {
    val source = readInput(System.`in`)

    val tokens = tokenize(source)
    if (tokens[0] != Token.BEGIN_TABLE) throw IllegalArgumentException("first tag is not <table>: ${tokens[0]}")

    val counts = ArrayList<Int>()
    val i = countCells(tokens, 1, counts)
    if (i != tokens.size) throw IllegalArgumentException("tags after end of table")

    counts.sorted().forEach { print("$it ") }
}
