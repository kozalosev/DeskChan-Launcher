package info.deskchan.launcher.cli

import info.deskchan.launcher.Localization
import java.util.*


open class Console(private val localization: Localization, private var writeToBuffer: Boolean = false) {

    private var lastWrittenString = ""
    private val buffer = mutableListOf<String>()
    private val console: java.io.Console? = System.console()


    fun log(obj: Any?) {
        if (obj is Throwable) {
            obj.printStackTrace()
        } else {
            System.err.println(obj.toString())
        }
    }

    fun write(text: String) = printLine(text.localized())

    fun info(text: String) = printLine(text.localized())

    fun warn(text: String) = printLine("! ${text.localized()}")

    fun important(text: String) {
        val prefix = if (lastWrittenString.endsWith("\n\n")) "" else "\n"
        printLine("$prefix${text.localized()}\n")
    }

    fun raw(text: String) = printRaw(text)

    fun blank(count: Int = 1) = printRaw("\n".repeat(count))

    fun update(text: String) {
        if (lastWrittenString.endsWith('\n')) {
            printRaw(text)
            return
        }

        val lastLength = lastWrittenString.length
        var newStr = "\r$text"
        val newLength = newStr.length
        if (lastLength > 0 && newLength < lastLength) {
            newStr += " ".repeat(lastLength - newLength)
        }

        if (writeToBuffer) {
            buffer.removeAt(buffer.lastIndex)
        }
        printRaw(newStr)
    }

    fun input() = console?.readLine() ?: ""

    fun input(question: String, options: List<String>): String {
        val localizedQuestion = question.localized()
        val query = when {
            options.isNotEmpty() -> "$localizedQuestion [${options.joinToString("/")}]: "
            else                 -> "$localizedQuestion: "
        }

        if (writeToBuffer) {
            throw IllegalStateException("Attempt to request user response with message \"$question\" while buffering is still enabled!")
        }
        printRaw(query)
        return input()
    }

    private fun printRaw(str: String) {
        lastWrittenString = str
        if (writeToBuffer) {
            buffer.add(str)
        } else {
            print(str)
        }
    }

    private fun printLine(str: String) = printRaw("$str\n")


    fun write(pattern: String, vararg vars: Any) = write(pattern.resolve(vars))

    fun info(pattern: String, vararg vars: Any) = info(pattern.resolve(vars))

    fun warn(pattern: String, vararg vars: Any) = warn(pattern.resolve(vars))

    fun important(pattern: String, vararg vars: Any) = important(pattern.resolve(vars))

    fun input(pattern: String, vararg vars: Any, options: List<String>) = input(pattern.resolve(vars), options)


    fun disableBuffering() {
        writeToBuffer = false
        val content = buffer.joinToString("")
        print(content)
    }

    fun enableBuffering() {
        writeToBuffer = true
    }

    fun resetBuffer() {
        val content = buffer.joinToString("")
        log(content)
        buffer.clear()
    }


    private fun String.resolve(vars: Array<out Any>): String {
        val pattern = this.localized()
        return try {
            pattern.format(*vars)
        } catch (e: IllegalFormatException) {
            warn("Invalid localization string!")
            log(e)
            listOf(pattern, *vars).toString()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.localized() = localization.getString(this)

}
