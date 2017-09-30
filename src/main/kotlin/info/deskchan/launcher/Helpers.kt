package info.deskchan.launcher

import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.zip.ZipException


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

fun copyLauncherTo(path: Path) {
    val allFiles = env.rootDirPath.toFile().listFiles()
    val coreFile = allFiles.filter { file -> file.name == "$CORE_FILENAME.jar" }
    val exeFiles = allFiles
            .filter { file -> file.extension == "exe" }
            .filter {
                val jar = try {
                    JarFile(it)
                } catch (e: ZipException) {
                    null
                }
                jar?.manifest?.mainAttributes?.getValue("Launcher-Module") != null
            }
    val exeFileNames = exeFiles.map { it.nameWithoutExtension }
    val shFiles = allFiles
            .filter { file -> file.extension.isEmpty() }
            .filter { file -> exeFileNames.contains(file.name) }

    (coreFile + exeFiles + shFiles).forEach {
        val newFile = path.resolve(it.name).toFile()
        it.copyTo(newFile, overwrite = true)
    }
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
