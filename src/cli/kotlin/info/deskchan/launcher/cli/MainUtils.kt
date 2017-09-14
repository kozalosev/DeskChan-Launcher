package info.deskchan.launcher.cli

import info.deskchan.launcher.LocalizationFactory
import info.deskchan.launcher.onWindows
import java.nio.file.Path
import java.nio.file.Paths


internal object getLocalizationFactory {
    private val localizationFactory by lazy { LocalizationFactory(javaClass.`package`.name) }
    operator fun invoke() = localizationFactory
}


internal object getLauncherExecFilePath {
    private val launcherExecFilePath: Path by lazy {
        val jarPath = Paths.get(javaClass.protectionDomain.codeSource.location.toURI())
        val extension = if (onWindows) ".exe" else ""
        jarPath.parent.resolve("$LAUNCHER_FILENAME$extension")
    }
    operator fun invoke() = launcherExecFilePath
}
