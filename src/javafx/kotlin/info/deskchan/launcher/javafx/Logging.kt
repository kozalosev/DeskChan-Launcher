package info.deskchan.launcher.javafx

import info.deskchan.launcher.env


private object Logger {

    private val file = env.rootDirPath.resolve("$LAUNCHER_FILENAME.log").toFile()

    init {
        // Clears the file at startup.
        if (file.exists()) {
            file.writeText("")
        }
    }

    fun log(str: String) = file.appendText("$str${System.lineSeparator()}")

}


internal fun log(message: String, vararg vars: Any) = Logger.log(message.localize(*vars))
internal fun log(error: Throwable)                  = Logger.log("! $error")
internal fun log(obj: Any?)                         = Logger.log(obj.toString())
