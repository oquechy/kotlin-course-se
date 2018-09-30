package ru.hse.spb

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser

@RunWith(MockitoJUnitRunner::class)
class InterpreterTest {

    private lateinit var interpreter: Interpreter

    @Mock
    private val a: Token
    private val b: Token
    private val c: Token

    @Before
    fun setUp() {
        interpreter = Interpreter()
    }

    @Test
    fun `test block without braces`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var a = 1
                    |var b = 2
                    |var c = a + b * 2 % 3 && a <= b || a == b
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        val scope = interpreter.getScope()
        assertThat(scope.loadValue("a"), equalTo(1))
        assertThat(scope.loadValue("b"), equalTo(2))
        assertThat(scope.loadValue("c"), equalTo(1))
    }

    @Test
    fun `test condition`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var s = 0
                    |if (4 - 2 * 2) {
                    |   s = 6
                    |} else {
                    |   s = 5
                    |}
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        val scope = interpreter.getScope()
        assertThat(scope.loadValue("s"), equalTo(5))
    }

    @Test
    fun `test loop`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var i = 0
                    |var s = 1
                    |while (i < 5) {
                    |   i = i + 1
                    |   s = s * 2
                    |}
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        val scope = interpreter.getScope()
        assertThat(scope.loadValue("s"), equalTo(32))
        assertThat(scope.loadValue("i"), equalTo(5))
    }

    @Test
    fun `test function`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """fun foo(n) {
                    |   return n >= 0
                    |}
                    |var a = foo(-1)
                    |var b = foo(a) || 1
                    |var c = foo(foo(0))
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        val scope = interpreter.getScope()
        assertThat(scope.loadValue("a"), equalTo(0))
        assertThat(scope.loadValue("b"), equalTo(1))
        assertThat(scope.loadValue("c"), equalTo(1))
    }

}