package ru.hse.spb

import java.io.OutputStream
import java.io.Writer
import java.nio.charset.Charset

interface Element {
    fun render(output: Writer, indent: String)
}

class TextElement(private val text: String) : Element {
    override fun render(output: Writer, indent: String) {
        output.write("$indent$text\n")
    }
}

class MathElement(private val formula: String) : Element {
    override fun render(output: Writer, indent: String) {
        output.write("$indent$$formula$\n")
    }
}

@DslMarker
annotation class TexMarker

@TexMarker
abstract class ControlElement(private val requiredArguments: ArrayList<String> = arrayListOf(), private val optionalArguments: ArrayList<String> = arrayListOf()) : Element {
    protected val children = arrayListOf<Element>()

    fun addArguments(arg: String) {
        requiredArguments += arg
    }

    protected fun <T : Element> initChild(element: T, init: T.() -> Unit) {
        element.init()
        children += element
    }

    fun renderArguments(output: Writer) {
        if (optionalArguments.isNotEmpty())
            output.write(optionalArguments.joinToString(prefix = "[", separator = ",", postfix = "]"))

        if (requiredArguments.isNotEmpty())
            output.write(requiredArguments.joinToString(prefix = "{", separator = ",", postfix = "}"))
    }

    infix fun String.to(value: String) = this + "=" + value
}

open class Command(private val name: String, arg: String? = null, vararg optional: String)
    : ControlElement(if (arg != null) arrayListOf(arg) else arrayListOf(), arrayListOf(*optional)) {
    override fun render(output: Writer, indent: String) {
        output.write("$indent\\$name")
        renderArguments(output)
        output.write("\n")
    }
}

open class Environment(protected val name: String, arg: String? = null, vararg optional: String)
    : ControlElement(if (arg != null) arrayListOf(arg) else arrayListOf(), arrayListOf(*optional)) {
    override fun render(output: Writer, indent: String) {
        output.write("$indent\\begin{$name}")
        renderArguments(output)
        output.write("\n")
        for (c in children) {
            c.render(output, "$indent  ")
        }
        output.write("$indent\\end{$name}\n")
    }
}

open class EnvironmentWithText(name: String, arg: String? = null, vararg optional: String)
    : Environment(name, arg, *optional) {
    operator fun String.unaryPlus() {
        children += TextElement(this)
    }

    operator fun String.not() {
        children += MathElement(this)
    }

    fun itemize(vararg optional: String, init: Itemize.() -> Unit) = initChild(Itemize(*optional), init)
    fun enumerate(vararg optional: String, init: Enumerate.() -> Unit) = initChild(Enumerate(*optional), init)
    fun center(init: Center.() -> Unit) = initChild(Center(), init)
    fun environment(name: String, arg: String? = null, vararg optional: String, init: EnvironmentWithText.() -> Unit) =
            initChild(EnvironmentWithText(name, arg, *optional), init)
}

class Document : EnvironmentWithText("document") {
    fun documentclass(type: String, vararg optional: String) = initChild(DocumentClass(type, *optional)) { }

    fun usepackage(name: String, vararg optional: String) = initChild(Package(name, *optional)) { }

    fun frame(title: String? = null, vararg optional: String, init: Frame.() -> Unit) =
            initChild(Frame(title, *optional), init)

    override fun render(output: Writer, indent: String) {
        val (documentClassOrPackages, other) = children.partition { it is DocumentClass || it is Package }
        for (c in documentClassOrPackages) {
            c.render(output, indent)
        }
        output.write("$indent\\begin{$name}")
        output.write("\n")
        for (c in other) {
            c.render(output, "$indent  ")
        }
        output.write("$indent\\end{$name}\n")
    }

    fun render(output: OutputStream) = output.bufferedWriter(Charset.defaultCharset()).use { render(it, "") }
}

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    return document.apply(init)
}

class DocumentClass(documentClass: String, vararg optional: String)
    : Command("documentclass", documentClass, optional = *optional)

class Package(packageName: String, vararg optional: String)
    : Command("usepackage", packageName, *optional)

class Item(label: String? = null)
    : EnvironmentWithText("item", optional = *if (label != null) arrayOf(label) else arrayOf())

class Frame(title: String? = null, vararg optional: String) : EnvironmentWithText("frame", title, *optional)
class Center : EnvironmentWithText("center")

class Itemize(vararg optional: String) : Environment("itemize", optional = *optional) {
    fun item(label: String? = null, init: Item.() -> Unit) = initChild(Item(label), init)
}

class Enumerate(vararg optional: String) : Environment("enumerate", optional = *optional) {
    fun item(label: String? = null, init: Item.() -> Unit) = initChild(Item(label), init)
}
