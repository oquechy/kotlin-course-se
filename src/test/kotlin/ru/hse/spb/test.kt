package ru.hse.spb

import com.google.common.truth.Truth.assertThat
import org.apache.commons.io.IOUtils
import org.junit.Test

class TestSource {

    companion object {
        val samples = arrayOf(
                "<table><tr><td></td></tr></table>\n",
                "<table>\n" +
                        "<tr>\n" +
                        "<td>\n" +
                        "<table><tr><td></td></tr><tr><td></\n" +
                        "td\n" +
                        "></tr><tr\n" +
                        "><td></td></tr><tr><td></td></tr></table>\n" +
                        "</td>\n" +
                        "</tr>\n" +
                        "</table>\n",
                "<table><tr><td>\n" +
                        "<table><tr><td>\n" +
                        "<table><tr><td>\n" +
                        "<table><tr><td></td><td></td>\n" +
                        "</tr><tr><td></td></tr></table>\n" +
                        "</td></tr></table>\n" +
                        "</td></tr></table>\n" +
                        "</td></tr></table>\n")

        val tokens = arrayOf(
                listOf(Token.BEGIN_TABLE, Token.CELL, Token.END_TABLE),
                listOf(
                        Token.BEGIN_TABLE, Token.CELL,
                        Token.BEGIN_TABLE, Token.CELL, Token.CELL, Token.CELL, Token.CELL,
                        Token.END_TABLE, Token.END_TABLE),
                listOf(
                        Token.BEGIN_TABLE, Token.CELL,
                        Token.BEGIN_TABLE, Token.CELL,
                        Token.BEGIN_TABLE, Token.CELL,
                        Token.BEGIN_TABLE, Token.CELL, Token.CELL, Token.CELL,
                        Token.END_TABLE, Token.END_TABLE, Token.END_TABLE, Token.END_TABLE))
        val counts = arrayOf(
                listOf(1),
                listOf(1, 4),
                listOf(1, 1, 1, 3))
    }


    @Test
    fun testReadInput() {
        samples.forEach {
            val inputStream = IOUtils.toInputStream(it, "UTF-8")
            assertThat(readInput(inputStream)).isEqualTo(it.replace("\n", ""))
        }
    }

    @Test
    fun testTokenize() {
        for ((i, it) in samples.withIndex()) {
            assertThat(tokenize(it.replace("\n", ""))).isEqualTo(tokens[i])
        }
    }

    @Test
    fun testCountCells() {
        for ((i, it) in tokens.withIndex()) {
            val result = mutableListOf<Int>()
            assertThat(countCells(it, 1, result)).isEqualTo(tokens[i].size)
            assertThat(result).containsExactlyElementsIn(counts[i])
        }
    }
}