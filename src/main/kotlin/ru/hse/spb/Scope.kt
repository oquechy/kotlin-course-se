package ru.hse.spb

import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

interface Scope {
    fun loadValue(identifier: TerminalNode): Int = throw NotInScopeException(identifier)

    fun loadFunction(identifier: TerminalNode): (List<Int>) -> Int = throw NotInScopeException(identifier)
}

class MutableScope(val parent: Scope? = null) : Scope {

    private val values = HashMap<String, Int?>()
    private val functions = HashMap<String, (List<Int>) -> Int>()

    fun storeValue(identifier: TerminalNode, value: Int?) {
        val name = identifier.text
        if (values.containsKey(name))
            throw AlreadyInScopeException(identifier)

        values[name] = value
    }

    fun updateValue(identifier: TerminalNode, value: Int?) {
        val name = identifier.text
        if (!values.containsKey(name))
            if (parent is MutableScope) parent.updateValue(identifier, value) else throw NotInScopeException(identifier)
        else values[name] = value
    }

    override fun loadValue(identifier: TerminalNode): Int =
            values[identifier.text] ?: parent?.loadValue(identifier) ?: throw NotInScopeException(identifier)

    fun storeFunction(identifier: TerminalNode, function: (List<Int>) -> Int) {
        val name = identifier.text
        if (functions.containsKey(name))
            throw AlreadyInScopeException(identifier)

        functions[name] = function
    }

    override fun loadFunction(identifier: TerminalNode): (List<Int>) -> Int =
            functions[identifier.text] ?: parent?.loadFunction(identifier) ?: throw NotInScopeException(identifier)
}

class AlreadyInScopeException(val identifier: TerminalNode)
    : Exception("${identifier.symbol.line}:${identifier.symbol.charPositionInLine} already in scope: $identifier")

class NotInScopeException(val identifier: TerminalNode)
    : Exception("${identifier.symbol.line}:${identifier.symbol.charPositionInLine} not in scope: $identifier")