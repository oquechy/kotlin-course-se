package ru.hse.spb

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Pass a filename as the only argument")
        return
    }

    val expLexer = ExpLexer(CharStreams.fromFileName(args[0]))
    val parser = ExpParser(BufferedTokenStream(expLexer))
    val file = parser.file()
    val interpreter = Interpreter(Library())
    interpreter.visit(file)
}