package ru.hse.spb

import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

typealias Arguments = List<Int>
typealias Executable = (Arguments) -> Int

interface Scope {
    fun loadValue(identifier: TerminalNode): Int = throw NotInScopeException(identifier)

    fun loadFunction(identifier: TerminalNode): Executable = throw NotInScopeException(identifier)
}

class MutableScope(val parent: Scope? = null) : Scope {

    private val values = HashMap<String, Int?>()
    private val functions = HashMap<String, Executable>()

    fun storeValue(identifier: TerminalNode, value: Int?) {
        val name = identifier.text
        if (name in values)
            throw AlreadyInScopeException(identifier)

        values[name] = value
    }

    fun updateValue(identifier: TerminalNode, value: Int?) {
        val name = identifier.text
        if (name !in values)
            if (parent is MutableScope) parent.updateValue(identifier, value) else throw NotInScopeException(identifier)
        else values[name] = value
    }

    override fun loadValue(identifier: TerminalNode): Int =
            values[identifier.text] ?: parent?.loadValue(identifier) ?: throw NotInScopeException(identifier)

    fun storeFunction(identifier: TerminalNode, function: Executable) {
        val name = identifier.text
        if (name in functions)
            throw AlreadyInScopeException(identifier)

        functions[name] = function
    }

    override fun loadFunction(identifier: TerminalNode): Executable =
            functions[identifier.text] ?: parent?.loadFunction(identifier) ?: throw NotInScopeException(identifier)
}

class AlreadyInScopeException(val identifier: TerminalNode)
    : Exception("${identifier.symbol.line}:${identifier.symbol.charPositionInLine} already in scope: $identifier")

class NotInScopeException(val identifier: TerminalNode)
    : Exception("${identifier.symbol.line}:${identifier.symbol.charPositionInLine} not in scope: $identifier")