package ru.hse.spb

import java.nio.charset.Charset

fun result() =
        document {
            documentclass("beamer")
            usepackage("babel", "english")
            usepackage("inputenc", "utf8x")
            usepackage("enumitem", "shortlabels")
            frame("title", "fragile" to "1", "shrink") {
                +"frame"
                !"x^y"
                enumerate { }
                +"txt"
            }
            frame {
                itemize("noitemsep", "label" to "a)") {
                    item {
                        +"wdef"
                        center {
                            +"efref"
                            !"e^e"
                        }
                    }
                    item {
                        +"efer"
                        environment("equation", "mai", "mai" to "cai") {}
                    }
                }
            }
        }


fun main(args: Array<String>) {
    System.out.bufferedWriter(Charset.defaultCharset())
            .use { result().render(System.out) }
}
