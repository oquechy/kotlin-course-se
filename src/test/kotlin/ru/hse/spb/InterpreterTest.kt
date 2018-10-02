package ru.hse.spb

import com.google.common.truth.Truth.assertThat
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.tree.TerminalNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser

@RunWith(MockitoJUnitRunner::class)
class InterpreterTest {

    private lateinit var interpreter: Interpreter
    private lateinit var output: MutableList<Int>

    inner class MockLibrary : Scope {

        private val functions = hashMapOf(
                Pair<String, (List<Int>) -> Int>("println", { args -> output.addAll(args); 0 }))

        override fun loadFunction(identifier: TerminalNode): ((List<Int>) -> Int) = functions[identifier.text]
                ?: throw RuntimeException("invalid function call $identifier")
    }

    @Before
    fun setUp() {
        output = ArrayList()
        interpreter = Interpreter(MockLibrary())
    }

    @Test
    fun `test block without braces`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var a = 1
                    |var b = 2
                    |var c = a + b * 2 % 3 && a <= b || a == b
                    |println(a, b, c)
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(1, 2, 1)
    }

    @Test
    fun `test condition with assignment`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var s = 0
                    |if (4 - 2 * 2) {
                    |   s = 6
                    |} else {
                    |   s = 5
                    |}
                    |println(s)
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(5)
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
                    |println(s, i)
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(32, 5)
    }

    @Test
    fun `test condition with println`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var a = 10
                    |var b = 20
                    |if (a > b) {
                    |    println(1)
                    |} else {
                    |    println(0)
                    |}""".trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(0)
    }

    @Test
    fun `test comment`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """fun foo(n) {
                        fun bar(m) {
                            return m + n
                        }
                        return bar(1)
                    }

                    println(foo(41)) // prints 42""".trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(42)
    }

    @Test
    fun `test fibonacci`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """fun fib(n) {
                    |    if (n <= 1) {
                    |        return 1
                    |    }
                    |    return fib(n - 1) + fib(n - 2)
                    |}
                    |
                    |var i = 1
                    |while (i <= 5) {
                    |    println(i, fib(i))
                    |    i = i + 1
                    |}""".trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(1, 1, 2, 2, 3, 3, 4, 5, 5, 8)
    }

    @Test
    fun `test function`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """fun foo(n) {
                    |   return n >= 0
                    |}
                    |var a = foo(-1)
                    |var b = (foo(a)) || 1
                    |var c = foo(foo(0))
                    |println(a, b, c)
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(0, 1, 1)
    }

    @Test
    fun `test shadowing`() {
        val expLexer = ExpLexer(CharStreams.fromString(
                """var n = 10
                    |fun foo(n) {
                    |   fun bar(n) {
                    |       fun baz(n) {
                    |           n = n + 1
                    |           return n
                    |       }
                    |       n = (baz(n)) + 1
                    |       return n
                    |   }
                    |   n = (bar(n)) + 1
                    |   return n
                    |}
                    |var a = foo(0)
                    |println(n, a)
                """.trimMargin()))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val tree = parser.file()
        interpreter.visit(tree)
        assertThat(output).containsExactly(10, 3)
    }
}