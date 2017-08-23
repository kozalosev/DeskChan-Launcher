package info.deskchan.installer

import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


private const val MANIFEST_FILENAME = "version.json"
private const val QUIT_DELAY = 5    // in seconds

val env = Environment()
val view: LocalizableTextView = LocalizableConsole(env.getLocalization())


fun main(args: Array<String>) {
    view.important("important.welcome")

    val resolver = VersionResolver(env.rootDirPath, MANIFEST_FILENAME)
    var quitDelayMultiplier = 1

    if (args.isNotEmpty()) {
        val response = when (args[0]) {
            "version" -> resolver.latestVersion.toString()
            "update-required" -> resolver.isUpdateNeeded.toString()
            else -> env.getString("unknown_command")
        }
        view.write(response)
        exitProcess(0)
    }

    val installedVersion = resolver.installedVersion
    if (installedVersion != null) {
        view.info("info.installed_version", installedVersion)
    }

    if (resolver.isUpdateNeeded) {
        view.info("info.installation_required")
        quitDelayMultiplier = 2

        val downloader: Installer = ZipInstaller(env.rootDirPath, MANIFEST_FILENAME)
        try {
            downloader.install(resolver.latestVersionUrl, resolver.latestVersion.toString())
        } catch (e: Exception) {
            view.important("important.installation_failed")
            view.log(e)
            exitProcess(1)
        }
    } else {
        view.info("info.latest_already_installed")
    }

    launchApplication()
    quitAfterDelay(QUIT_DELAY * quitDelayMultiplier)
}


private fun getBinFilePath(): Path {
    val onWindows = System.getProperty("os.name").startsWith("Windows")
    val extension = if (onWindows) ".exe" else ""
    val binFilePath = env.rootDirPath.resolve("bin/DeskChan$extension")
    binFilePath.toFile().setExecutable(true)
    return binFilePath
}

private fun launchApplication() {
    val binFilePath = getBinFilePath()
    if (Files.isExecutable(binFilePath)) {
        view.important("important.launching")
        ProcessBuilder(binFilePath.toString()).start()
    } else {
        view.important("important.could_not_find_exe", binFilePath)
    }
}

private fun quitAfterDelay(quitDelay: Number) {
    view.important("important.window_will_be_closed", quitDelay)
    Timer().schedule(quitDelay.toLong() * 1000) {
        exitProcess(0)
    }
}
