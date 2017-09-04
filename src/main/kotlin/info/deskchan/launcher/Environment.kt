package info.deskchan.launcher

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class Environment {

    val rootDirPath: Path by lazy {
        val executableDir = Paths.get(javaClass.protectionDomain.codeSource.location.toURI())
        var directoryPath = executableDir.parent
        while (!Files.isDirectory(directoryPath)) {
            directoryPath = directoryPath.parent
        }
        directoryPath
    }

    val version by lazy { javaClass.`package`.implementationVersion ?: "[UNOFFICIAL DEBUG VERSION]" }

}


val onWindows by lazy { System.getProperty("os.name").startsWith("Windows") }
