package info.deskchan.installer

import java.util.*


interface TextView {
    fun log(obj: Any?)

    fun write(text: String)
    fun info(text: String)
    fun warn(text: String)
    fun important(text: String)
    fun raw(text: String)
    fun blank(count: Int = 1)
    fun update(text: String)

    fun input(): String
    fun input(question: String, options: List<String> = emptyList()): String
}

interface LocalizableTextView : TextView {
    fun write(pattern: String, vararg vars: Any)
    fun info(pattern: String, vararg vars: Any)
    fun warn(pattern: String, vararg vars: Any)
    fun important(pattern: String, vararg vars: Any)

    fun input(pattern: String, vararg vars: Any, options: List<String> = emptyList()): String
}


open class Console : TextView {

    private var lastWrittenString: String = ""
    private val console: java.io.Console? = System.console()


    override fun log(obj: Any?) {
        if (obj is Throwable) {
            obj.printStackTrace()
        } else {
            System.err.println(obj.toString())
        }
    }

    override fun write(text: String) = printLine(text)

    override fun info(text: String) = printLine(text)

    override fun warn(text: String) = printLine("! $text")

    override fun important(text: String) {
        val prefix = if (lastWrittenString.endsWith('\n')) "" else "\n"
        printLine("$prefix$text\n")
    }

    override fun raw(text: String) = printRaw(text)

    override fun blank(count: Int) = printRaw("\n".repeat(count))

    override fun update(text: String) {
        val lastLength = lastWrittenString.length
        var newStr = "\r$text"
        val newLength = newStr.length
        if (lastLength > 0 && newLength < lastLength) {
            newStr += " ".repeat(lastLength - newLength)
        }
        printRaw(newStr)
    }

    override fun input() = console?.readLine() ?: ""

    override fun input(question: String, options: List<String>): String {
        val query = when {
            options.isNotEmpty() -> "$question [${options.joinToString("/")}]: "
            else                 -> "$question: "
        }
        printRaw(query)
        return input()
    }

    private fun printRaw(str: String) {
        lastWrittenString = str
        print(str)
    }

    private fun printLine(str: String) {
        lastWrittenString = str
        println(str)
    }

}


class LocalizableConsole(private val localization: Localization) : Console(), LocalizableTextView {

    override fun write(text: String) = super.write(localization.getString(text))

    override fun info(text: String) = super.info(localization.getString(text))

    override fun warn(text: String) = super.warn(localization.getString(text))

    override fun important(text: String) = super.important(localization.getString(text))

    override fun input(question: String, options: List<String>) = super.input(localization.getString(question), options)


    override fun write(pattern: String, vararg vars: Any) = super.write(pattern.resolve(vars))

    override fun info(pattern: String, vararg vars: Any) = super.info(pattern.resolve(vars))

    override fun warn(pattern: String, vararg vars: Any) = super.warn(pattern.resolve(vars))

    override fun important(pattern: String, vararg vars: Any) = super.important(pattern.resolve(vars))

    override fun input(pattern: String, vararg vars: Any, options: List<String>) = super.input(pattern.resolve(vars), options)

    private fun String.resolve(vars: Array<out Any>): String {
        val pattern = localization.getString(this)
        return try {
            pattern.format(*vars)
        } catch (e: IllegalFormatException) {
            super.warn("Invalid localization string!")
            log(e)
            listOf(pattern, *vars).toString()
        }
    }

}
