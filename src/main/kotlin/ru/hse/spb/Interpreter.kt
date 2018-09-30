package ru.hse.spb

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import ru.hse.spb.parser.ExpBaseVisitor
import ru.hse.spb.parser.ExpParser

class Interpreter : ExpBaseVisitor<Int?>() {

    private var scope = MutableScope()
    fun getScope(): Scope = scope

    private var returnValue: Int? = null
    fun getReturnValue(): Int? = returnValue

    override fun visitFile(ctx: ExpParser.FileContext): Int? = visit(ctx.block())

    override fun visitBlock(ctx: ExpParser.BlockContext): Int? {
        ctx.statement().forEach { if (returnValue != null) return returnValue else visit(it) }
        return null
    }

    override fun visitStatement(ctx: ExpParser.StatementContext): Int? = visit(ctx.children.first())

    override fun visitAssignment(ctx: ExpParser.AssignmentContext): Int? {
        val value = ensureVisit(ctx.expression())
        scope.updateValue(ctx.Identifier(), value)
        return null
    }

    override fun visitVariable(ctx: ExpParser.VariableContext): Int? {
        if (ctx.expression() != null) {
            val value = ensureVisit(ctx.expression())
            scope.storeValue(ctx.Identifier(), value)
        } else {
            scope.storeValue(ctx.Identifier(), null)
        }
        return null
    }

    override fun visitFunction(ctx: ExpParser.FunctionContext): Int? {
        scope.storeFunction(ctx.Identifier(), ctx)
        return null
    }

    override fun visitExpression(ctx: ExpParser.ExpressionContext): Int? = visit(ctx.children.first())

    override fun visitWhileLoop(ctx: ExpParser.WhileLoopContext): Int? {
        while (ensureVisit(ctx.expression()).boolean)
            visit(ctx.blockWithBraces())
        return null
    }

    override fun visitBlockWithBraces(ctx: ExpParser.BlockWithBracesContext): Int? {
        scope = MutableScope(scope)
        visit(ctx.block())
        scope = scope.parent ?: throw RuntimeException("${ctx.start.line}:${ctx.start.charPositionInLine} end of global scope reached")
        return null
    }

    override fun visitCondition(ctx: ExpParser.ConditionContext): Int? {
        val cases = ctx.blockWithBraces()
        return if (ensureVisit(ctx.expression()).boolean) visit(cases.first()) else visit(cases.last())
    }

    override fun visitReturnStatement(ctx: ExpParser.ReturnStatementContext): Int? = ensureVisit(ctx.expression()).also { returnValue = it }

    override fun visitOrExpression(ctx: ExpParser.OrExpressionContext): Int? {
        if (ctx.orExpression() == null)
            return ensureVisit(ctx.andExpression())
        val operation = toOperation(ctx.OrOp(), ctx)
        return operation(ensureVisit(ctx.andExpression()), ensureVisit(ctx.orExpression()))
    }

    override fun visitAndExpression(ctx: ExpParser.AndExpressionContext): Int? {
        if (ctx.andExpression() == null)
            return ensureVisit(ctx.relationalExpression())
        val operation = toOperation(ctx.AndOp(), ctx)
        return operation(ensureVisit(ctx.relationalExpression()), ensureVisit(ctx.andExpression()))
    }

    override fun visitRelationalExpression(ctx: ExpParser.RelationalExpressionContext): Int? {
        if (ctx.relationalExpression() == null)
            return ensureVisit(ctx.additiveExpression())
        val operation = toOperation(ctx.RelationalOp(), ctx)
        return operation(ensureVisit(ctx.additiveExpression()), ensureVisit(ctx.relationalExpression()))
    }

    override fun visitAdditiveExpression(ctx: ExpParser.AdditiveExpressionContext): Int? {
        if (ctx.additiveExpression() == null)
            return ensureVisit(ctx.multiplicativeExpression())
        val operation = toOperation(ctx.AdditiveOp(), ctx)
        return operation(ensureVisit(ctx.multiplicativeExpression()), ensureVisit(ctx.additiveExpression()))
    }

    override fun visitMultiplicativeExpression(ctx: ExpParser.MultiplicativeExpressionContext): Int? {
        if (ctx.multiplicativeExpression() == null)
            return ensureVisit(ctx.atomExpression())
        val operation = toOperation(ctx.MultiplicativeOp(), ctx)
        return operation(ensureVisit(ctx.atomExpression()), ensureVisit(ctx.multiplicativeExpression()))
    }

    override fun visitAtomExpression(ctx: ExpParser.AtomExpressionContext): Int? = ensureVisit(ctx.children.first() as ParserRuleContext)

    override fun visitLiteral(ctx: ExpParser.LiteralContext): Int? = ctx.text.toInt()

    override fun visitIdentifier(ctx: ExpParser.IdentifierContext): Int? = scope.loadValue(ctx.Identifier())

    override fun visitExpressionWithBraces(ctx: ExpParser.ExpressionWithBracesContext): Int? = ensureVisit(ctx.expression())

    override fun visitFunctionCall(ctx: ExpParser.FunctionCallContext): Int? {
        val function = scope.loadFunction(ctx.Identifier())
        val values = ctx.arguments().expression().map { ensureVisit(it) }
        function.parameterNames().Identifier().zip(values).forEach { scope.storeValue(it.first, it.second) }
        visit(function.blockWithBraces())
        return returnValue.also { returnValue = null }
    }

    private fun toOperation(token: TerminalNode, ctx: ParserRuleContext): (Int, Int) -> Int = when (token.text) {
        "*" -> Int::times
        "/" -> Int::div
        "%" -> Int::rem
        "+" -> Int::plus
        "-" -> Int::minus
        ">" -> { x, y -> (x > y).int }
        "<" -> { x, y -> (x < y).int }
        ">=" -> { x, y -> (x >= y).int }
        "<=" -> { x, y -> (x <= y).int }
        "==" -> { x, y -> (x == y).int }
        "!=" -> { x, y -> (x != y).int }
        "&&" -> { x, y -> (x.boolean && y.boolean).int }
        "||" -> { x, y -> (x.boolean || y.boolean).int }
        else -> throw RuntimeException("${ctx.start.line}:${ctx.start.charPositionInLine} unknown operation $token")
    }

    private fun ensureVisit(ctx: ParserRuleContext) =
            visit(ctx) ?: throw ReturnValueException(ctx)
}

private val Boolean.int: Int
    get() = if (this) 1 else 0

private val Int.boolean: Boolean
    get() = this != 0

class ReturnValueException(ctx: ParserRuleContext)
    : RuntimeException("${ctx.start.line}:${ctx.start.charPositionInLine} expression should return a value: $ctx")
