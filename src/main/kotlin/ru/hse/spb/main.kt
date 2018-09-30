package ru.hse.spb

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser

fun main(args: Array<String>) {
    val expLexer = ExpLexer(CharStreams.fromString(
            """var a = 10"""
                    .trimMargin()))
    val parser = ExpParser(BufferedTokenStream(expLexer))
    val file = parser.file()
    print(file.toStringTree(parser))
    val interpreter = Interpreter()
    interpreter.visit(file)
}