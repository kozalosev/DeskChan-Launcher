package info.deskchan.launcher


enum class ExitStatus(val code: Int) {
    OK(0),
    ILLEGAL_ARGUMENTS(1),
    INVALID_LOCALE(2),
    INVALID_INSTALLATION_PATH(3),
    INSTALLATION_FAILED(4),
    EXECUTABLE_NOT_FOUND(5)
}
