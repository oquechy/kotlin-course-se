package ru.hse.spb

import org.antlr.v4.runtime.tree.TerminalNode

class Library : Scope {

    private val functions = hashMapOf(
            Pair<String, (List<Int>) -> Int>("println", { args -> println(args.joinToString(separator = " ")); 0 }))

    override fun loadFunction(identifier: TerminalNode): ((List<Int>) -> Int)
            = functions[identifier.text] ?: throw NotInScopeException(identifier)
}