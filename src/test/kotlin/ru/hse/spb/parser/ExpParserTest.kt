package ru.hse.spb.parser

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class ExpParserTest {

    @Test
    fun `parse function definition`() {
        val expLexer = ExpLexer(CharStreams.fromString("fun foo(a, b) { return 0 }"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(function fun foo ( (parameterNames a , b) ) {
                    |(block
                    |(statement
                    |(returnStatement return
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(literal 0)))))))))))
                    |}))))""".trimMargin().replace('\n', ' ')))
    }

    @Test
    fun `parse expression`() {
        val expLexer = ExpLexer(CharStreams.fromString("x + foo() && 1 <= (y)"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(identifier x)))
                    |+
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(functionCall foo ( arguments )))))))
                    |&&
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(literal 1))))
                    |<=
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(expressionWithBraces (
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(identifier y))))))))
                    |))))))))))))))""".trimMargin().replace('\n', ' ')))
    }

    @Test
    fun `parse variable definition`() {
        val expLexer = ExpLexer(CharStreams.fromString("var x = 0"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(variable var x =
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression (literal 0))))))))))))""".trimMargin().replace('\n', ' ')))
    }

    @Test
    fun `parse comment`() {
        val commentLexer = ExpLexer(CharStreams.fromString(
                """var x = 0 // comment
                    | var a = b
                """.trimMargin()))
        val noCommentLexer = ExpLexer(CharStreams.fromString(
                """var x = 0
                    | var a = b
                """.trimMargin()))
        val commentParser = ExpParser(BufferedTokenStream(commentLexer))
        val noCommentParser = ExpParser(BufferedTokenStream(noCommentLexer))
        val commentFile = commentParser.file()
        val noCommentFile = noCommentParser.file()
        println(commentFile.toStringTree(commentParser))
        assertThat(commentFile.toStringTree(commentParser), equalTo(noCommentFile.toStringTree(noCommentParser)))
    }

    @Test
    fun `parse variable assignment`() {
        val expLexer = ExpLexer(CharStreams.fromString("x = 4 + foo()"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(assignment x =
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(literal 4)))
                    |+
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(functionCall foo ( arguments ))))))))))))))""".trimMargin().replace('\n', ' ')))
    }

    @Test
    fun `parse return statement`() {
        val expLexer = ExpLexer(CharStreams.fromString("return foo() % 1"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(returnStatement return
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(functionCall foo ( arguments )))
                    |%
                    |(multiplicativeExpression
                    |(atomExpression
                    |(literal 1)))))))))))))""".trimMargin().replace('\n', ' ')))
    }

    @Test
    fun `parse branching`() {
        val expLexer = ExpLexer(CharStreams.fromString("if (foo()) { 1 } else { x }"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(condition if (
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(functionCall foo ( arguments )))))))))
                    |)
                    |(blockWithBraces {
                    |(block
                    |(statement
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(literal 1))))))))))
                    |}) else
                    |(blockWithBraces {
                    |(block
                    |(statement
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(identifier x))))))))))
                    |})))))""".trimMargin().replace('\n', ' ')))
    }

    @Test
    fun `parse while loop`() {
        val expLexer = ExpLexer(CharStreams.fromString("while (i != 0) { i = i - 1 }"))
        val parser = ExpParser(BufferedTokenStream(expLexer))
        val file = parser.file()
        println(file.toStringTree(parser))
        assertThat(file.toStringTree(parser), equalTo(
                """(file
                    |(block
                    |(statement
                    |(whileLoop while (
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(identifier i))))
                    |!=
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(literal 0)))))))))
                    |)
                    |(blockWithBraces {
                    |(block
                    |(statement
                    |(assignment i =
                    |(expression
                    |(orExpression
                    |(andExpression
                    |(relationalExpression
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression
                    |(identifier i)))
                    |-
                    |(additiveExpression
                    |(multiplicativeExpression
                    |(atomExpression (literal 1))))))))))))
                    |})))))""".trimMargin().replace('\n', ' ')))
    }
}