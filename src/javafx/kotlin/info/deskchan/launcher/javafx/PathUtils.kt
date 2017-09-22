package info.deskchan.launcher.javafx

import info.deskchan.launcher.onWindows
import java.nio.file.Path
import java.nio.file.Paths


internal object getLauncherExecFilePath {
    private val launcherExecFilePath: Path by lazy {
        val jarPath = Paths.get(javaClass.protectionDomain.codeSource.location.toURI())
        getLauncherExecFilePath(jarPath.parent)
    }
    operator fun invoke() = launcherExecFilePath
}

internal fun getLauncherExecFilePath(root: Path): Path {
    val extension = if (onWindows) ".exe" else ""
    return root.resolve("$LAUNCHER_FILENAME$extension")
}
