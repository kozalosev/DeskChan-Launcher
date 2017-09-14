package info.deskchan.launcher

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class Environment {

    val rootDirPath: Path by lazy {
        var directoryPath = Paths.get(javaClass.protectionDomain.codeSource.location.toURI()).parent
        while (!Files.isDirectory(directoryPath)) {
            directoryPath = directoryPath.parent
        }
        directoryPath
    }

    val version = javaClass.`package`.implementationVersion ?: "[UNOFFICIAL DEBUG VERSION]"

}


val onWindows = System.getProperty("os.name").startsWith("Windows")
