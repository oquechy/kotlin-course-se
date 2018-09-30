package ru.hse.spb

import org.antlr.v4.runtime.tree.TerminalNode
import ru.hse.spb.parser.ExpParser
import java.util.*

interface Scope {
    fun loadValue(identifier: TerminalNode): Int
    fun loadFunction(identifier: TerminalNode): ExpParser.FunctionContext
}

class MutableScope(val parent: MutableScope? = null) : Scope {

    private val values = HashMap<String, Int?>()
    private val functions = HashMap<String, ExpParser.FunctionContext>()

    fun storeValue(identifier: TerminalNode, value: Int?) {
        val name = identifier.text
        if (values.containsKey(name))
            throw AlreadyInScopeException(identifier)

        values[name] = value
    }

    fun updateValue(identifier: TerminalNode, value: Int?) {
        val name = identifier.text
        if (!values.containsKey(name))
            parent?.updateValue(identifier, value) ?: throw NotInScopeException(identifier)

        values[name] = value
    }

    override fun loadValue(identifier: TerminalNode): Int =
            values[identifier.text] ?: parent?.loadValue(identifier) ?: throw NotInScopeException(identifier)

    fun storeFunction(identifier: TerminalNode, function: ExpParser.FunctionContext) {
        val name = identifier.text
        if (functions.containsKey(name))
            throw AlreadyInScopeException(identifier)

        functions[name] = function
    }

    override fun loadFunction(identifier: TerminalNode): ExpParser.FunctionContext =
            functions[identifier.text] ?: parent?.loadFunction(identifier) ?: throw NotInScopeException(identifier)
}

class AlreadyInScopeException(val identifier: TerminalNode)
    : Exception("${identifier.symbol.line}:${identifier.symbol.charPositionInLine} already in scope: $identifier")

class NotInScopeException(val identifier: TerminalNode)
    : Exception("${identifier.symbol.line}:${identifier.symbol.charPositionInLine} not in scope: $identifier")