package info.deskchan.installer

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


private const val MANIFEST_FILENAME = "version.json"
private const val QUIT_DELAY = 5    // in seconds

private const val APPLICATION_NAME = "DeskChan"
private const val APPLICATION_WEBSITE = "https://deskchan.info"
private const val DEFAULT_DOWNLOAD_URL = "$APPLICATION_WEBSITE/deskchan.zip"
private const val DEFAULT_LAUNCHER_REPOSITORY_URL = "https://github.com/kozalosev/DeskChan-Launcher"

private val APPLICATION_REPOSITORY = Repository(APPLICATION_NAME, APPLICATION_NAME)
private val LAUNCHER_REPOSITORY = Repository("kozalosev", "$APPLICATION_NAME-Launcher")

val env = Environment()
val view: LocalizableTextView = LocalizableConsole(env.getLocalization())

private val execFilePath: Path by lazy {
    val extension = if (onWindows) ".exe" else ""
    env.rootDirPath.resolve("bin/$APPLICATION_NAME$extension")
}


fun main(args: Array<String>) {
    view.important("$APPLICATION_NAME Launcher ${env.version}\n$APPLICATION_WEBSITE")
    checkLauncherActuality()

    val resolver = ManifestVersionResolver(APPLICATION_REPOSITORY, env.rootDirPath, MANIFEST_FILENAME)
    var quitDelayMultiplier = 1

    if (args.isNotEmpty()) {
        val response = when (args[0]) {
            "version" -> resolver.installedVersion.toString()
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
        val url = resolver.latestVersionUrl ?: URL(DEFAULT_DOWNLOAD_URL)
        try {
            downloader.install(url, resolver.latestVersion.toString())
        } catch (e: Exception) {
            view.important("important.installation_failed")
            view.log(e)
            exitProcess(1)
        }

        setAutorunUp()
    } else {
        view.info("info.latest_already_installed")
    }

    launchApplication()
    quitAfterDelay(QUIT_DELAY * quitDelayMultiplier)
}


private fun checkLauncherActuality() {
    val resolver = StringVersionResolver(LAUNCHER_REPOSITORY, env.version)
    if (resolver.isUpdateNeeded) {
        val url = resolver.latestVersionUrl ?: URL(DEFAULT_LAUNCHER_REPOSITORY_URL)
        view.warn("warn.new_launcher_available", url)
    }
}

private fun setAutorunUp() {
    val answer = view.input("input.should_run_at_startup", listOf("Y", "N"))
    if (answer.isNotEmpty() && answer[0].toLowerCase() == 'y') {
        getAutorunManager(APPLICATION_NAME, execFilePath).setAutorunUp()
    }
}

private fun launchApplication() {
    execFilePath.toFile().setExecutable(true)
    if (Files.isExecutable(execFilePath)) {
        view.important("important.launching")
        ProcessBuilder(execFilePath.toString()).start()
    } else {
        view.important("important.could_not_find_executable", execFilePath)
    }
}

private fun quitAfterDelay(quitDelay: Number) {
    view.important("important.window_will_be_closed", quitDelay)
    Timer().schedule(quitDelay.toLong() * 1000) {
        exitProcess(0)
    }
}
