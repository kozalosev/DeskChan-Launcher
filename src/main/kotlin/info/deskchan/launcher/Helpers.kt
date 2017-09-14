package info.deskchan.launcher

import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


fun getExecFilePath(rootDirPath: Path): Path {
    val extension = if (onWindows) ".exe" else ""
    return rootDirPath.resolve("bin/$APPLICATION_NAME$extension")
}

fun setAutorunUp(execFilePath: Path) {
    execFilePath.toFile().setExecutable(true)
    getAutorunManager(APPLICATION_NAME, execFilePath).setAutorunUp()
}

fun resetAutorun(execFilePath: Path) {
    getAutorunManager(APPLICATION_NAME, execFilePath).resetAutorun()
}

@Throws(IOException::class)
fun launchApplication(execFilePath: Path) {
    execFilePath.toFile().setExecutable(true)
    if (Files.isExecutable(execFilePath)) {
        ProcessBuilder(execFilePath.toString()).start()
    } else {
        throw FileNotFoundException()
    }
}

fun exitProcess(status: ExitStatus): Nothing = kotlin.system.exitProcess(status.code)
