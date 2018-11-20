package ru.hse.spb

import org.antlr.v4.runtime.tree.TerminalNode

class Library : Scope {

    private val functions = hashMapOf(
            "println" to { args: Arguments -> println(args.joinToString(separator = " ")); 0 })

    override fun loadFunction(identifier: TerminalNode): Executable = functions[identifier.text]
            ?: throw NotInScopeException(identifier)
}