package ru.hse.spb

import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.charset.Charset

class MarktexTest {

    private lateinit var input: PipedInputStream
    private lateinit var output: PipedOutputStream

    @Before
    fun setUp() {
        input = PipedInputStream()
        output = PipedOutputStream(input)
    }

    @Test
    fun testText() {
        document {
            +"hi"
            +"my name \\name{name}"
            +"  is opiuy997@#$%\\"
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                        |  hi
                                        |  my name \name{name}
                                        |    is opiuy997@#$%\
                                        |\end{document}
                                        |""".trimMargin()))
    }

    @After
    fun tearDown() {
        input.close()
        output.close()
    }

    @Test
    fun testMath() {
        document {
            !"h^i"
            !"\\sum\\limits_{a}^{b}"
            !"x + y = z"
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                    |  ${'$'}h^i${'$'}
                                    |  ${'$'}\sum\limits_{a}^{b}${'$'}
                                    |  ${'$'}x + y = z${'$'}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testFrame() {
        document {
            frame {
                +"text#1"
                !"x^y"
                enumerate { }
            }
            frame("title", "limit" to "1", "shrink") {
                center { }
                itemize { }
            }
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                    |  \begin{frame}
                                    |    text#1
                                    |    ${'$'}x^y${'$'}
                                    |    \begin{enumerate}
                                    |    \end{enumerate}
                                    |  \end{frame}
                                    |  \begin{frame}[limit=1,shrink]{title}
                                    |    \begin{center}
                                    |    \end{center}
                                    |    \begin{itemize}
                                    |    \end{itemize}
                                    |  \end{frame}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testUsepackage() {
        document {
            usepackage("babel")
            usepackage("babel", "english", "russian")
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\usepackage{babel}
                                    |\usepackage[english,russian]{babel}
                                    |\begin{document}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testDocumentclass() {
        document {
            documentclass("beamer")
            documentclass("beamer", "12pt", "width" to "\\listwidth")
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\documentclass{beamer}
                                    |\documentclass[12pt,width=\listwidth]{beamer}
                                    |\begin{document}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testItemize() {
        document {
            itemize { }
            itemize("itemindent" to "1pt", "label" to "I") {
                item { +"txt" }
                item { !"f^ormule" }
                item { }
            }
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                    |  \begin{itemize}
                                    |  \end{itemize}
                                    |  \begin{itemize}[itemindent=1pt,label=I]
                                    |    \begin{item}
                                    |      txt
                                    |    \end{item}
                                    |    \begin{item}
                                    |      ${'$'}f^ormule${'$'}
                                    |    \end{item}
                                    |    \begin{item}
                                    |    \end{item}
                                    |  \end{itemize}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testEnumerate() {
        document {
            enumerate { }
            enumerate("noitemsep", "font" to "\\sfseries") {
                item { +"txt" }
                item { !"f^ormule" }
                item { }
            }
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                    |  \begin{enumerate}
                                    |  \end{enumerate}
                                    |  \begin{enumerate}[noitemsep,font=\sfseries]
                                    |    \begin{item}
                                    |      txt
                                    |    \end{item}
                                    |    \begin{item}
                                    |      ${'$'}f^ormule${'$'}
                                    |    \end{item}
                                    |    \begin{item}
                                    |    \end{item}
                                    |  \end{enumerate}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testAlign() {
        document {
            center {
                +"text#1"
                !"x^y"
                enumerate { }
                center { }
                itemize { }
            }
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                    |  \begin{center}
                                    |    text#1
                                    |    ${'$'}x^y${'$'}
                                    |    \begin{enumerate}
                                    |    \end{enumerate}
                                    |    \begin{center}
                                    |    \end{center}
                                    |    \begin{itemize}
                                    |    \end{itemize}
                                    |  \end{center}
                                    |\end{document}
                                    |""".trimMargin()))
    }

    @Test
    fun testEnvironment() {
        document {
            environment("pyglist", null, "language" to "kotlin") {
                +"""|val a = 1
                    |""".trimMargin()
            }
        }.render(output)
        val text = input.bufferedReader(Charset.defaultCharset()).readText()
        println(text)
        assertThat(text, equalTo("""\begin{document}
                                    |  \begin{pyglist}[language=kotlin]
                                    |    val a = 1
                                    |
                                    |  \end{pyglist}
                                    |\end{document}
                                    |""".trimMargin()))
    }
}