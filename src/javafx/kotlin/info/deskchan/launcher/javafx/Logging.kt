package info.deskchan.launcher.javafx


internal fun log(message: String, vararg vars: Any) = println(message.localize(*vars))
internal fun log(error: Throwable)                  = System.err.println(error)
internal fun log(obj: Any?)                         = println(obj.toString())
