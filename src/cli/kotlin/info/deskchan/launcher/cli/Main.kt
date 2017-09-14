package info.deskchan.launcher.cli

import info.deskchan.launcher.*
import info.deskchan.launcher.versioning.VersionResolver
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


internal const val LAUNCHER_FILENAME = "dcl"

private val localizationFactory = getLocalizationFactory()
internal var localization = localizationFactory.getLocalization()

internal var view = Console(localization, writeToBuffer = true)
    private set


// MAIN BLOCK

fun main(args: Array<String>) {
    val settings = parseArguments(args)
    val installationPath = settings.installationPath.resolve(APPLICATION_NAME)
    var launcherExecFilePath = getLauncherExecFilePath()

    checkLocale(settings.locale)

    val deskChanResolver = getDeskChanVersionResolver(installationPath)
    val selfResolver = getLauncherVersionResolver()

    with (settings) {
        when {
            justShowDeskChanVersion -> deskChanResolver.installedVersion
            justShowLauncherVersion -> selfResolver.installedVersion
            justShowDeskChanUpdateRequired -> deskChanResolver.isUpdateNeeded
            justShowLauncherUpdateRequired -> selfResolver.isUpdateNeeded
            else -> null
        }
    }?.let {
        view.resetBuffer()
        view.disableBuffering()
        view.write(it.toString())
        exitProcess(ExitStatus.OK)
    }

    view.disableBuffering()

    view.important("$APPLICATION_NAME Launcher ${env.version}\n$APPLICATION_WEBSITE")
    if (selfResolver.isUpdateNeeded) {
        val url = selfResolver.latestVersionUrl ?: URL(DEFAULT_LAUNCHER_REPOSITORY_URL)
        view.warn("warn.new_launcher_available", url)
    }
    view.info("info.working_directory", env.rootDirPath)

    val installedVersion = deskChanResolver.installedVersion
    installedVersion?.let {
        view.info("info.installed_version", it)
    }

    if (deskChanResolver.isUpdateNeeded) {
        if (!install(settings, deskChanResolver, installationPath)) {
            return
        }
        if (settings.installationPath != env.rootDirPath) {
            launcherExecFilePath = copyLauncherAndGetExecutable(settings.installationPath) ?: return
        }
    } else {
        view.info("info.latest_already_installed")
    }

    when {
        settings.autorun -> enableAutorun(launcherExecFilePath)
        settings.noAutorun -> disableAutorun(launcherExecFilePath)
        deskChanResolver.isUpdateNeeded -> if (askUser("input.should_run_at_startup")) {
            enableAutorun(launcherExecFilePath)
        }
    }

    view.important("important.launching")
    val execFilePath = getExecFilePath(installationPath)
    try {
        launchApplication(execFilePath)
    } catch (e: FileNotFoundException) {
        view.important("important.could_not_find_executable", execFilePath.toString())
        view.log(e)
    }
    exitAfterDelay(settings.delay)
}

// END MAIN BLOCK


private fun copyLauncherAndGetExecutable(path: Path): Path? {
    var launcherExecFilePath: Path? = null
    env.rootDirPath.toFile()
            .listFiles { file, _ -> file.nameWithoutExtension in listOf(CORE_FILENAME, LAUNCHER_FILENAME) }
            .forEach {
                val newFile = path.resolve(it.name).toFile()
                it.copyTo(newFile, overwrite = true)
                if (it.name == getLauncherExecFilePath().fileName.toString()) {
                    launcherExecFilePath = it.toPath()
                }
            }
    if (launcherExecFilePath == null) {
        view.important("important.no_launcher_after_copying")
    }
    return launcherExecFilePath
}

private fun checkLocale(tag: String, useBufferedView: Boolean = false) {
    if (localization.languageTag == tag) {
        return
    }

    try {
        val locale = Locale.Builder()
                .setLanguageTag(tag)
                .build()
        Locale.setDefault(locale)
        localization = localizationFactory.getSpecificLocalization(locale)
        view = Console(localization, writeToBuffer = useBufferedView)
    } catch (e: IllformedLocaleException) {
        view.log(e)
        view.important("important.invalid_locale")
        exitAfterDelay(5, ExitStatus.INVALID_LOCALE)
    }
}

private fun install(settings: Settings, deskChanResolver: VersionResolver, installationPath: Path): Boolean {
    view.info("info.installation_required")
    view.info("info.installation_directory", installationPath.toString())
    settings.delay *= 2

    val downloader: Installer
    try {
        downloader = ZipInstaller(installationPath, MANIFEST_FILENAME, getEventListeners())
    } catch (e: IOException) {
        val message = when (e) {
            is NotDirectoryException -> "not_directory"
            is CouldNotCreateDirectoryException -> "could_not_create_directory"
            else -> "invalid_installation_path"
        }
        view.important("important.$message")
        view.log(e)
        exitAfterDelay(settings.delay, ExitStatus.INVALID_INSTALLATION_PATH)
        return false
    }
    val url = deskChanResolver.latestVersionUrl ?: URL(DEFAULT_DOWNLOAD_URL)
    val version = deskChanResolver.latestVersion.toString()
    view.info("info.install_version", version)
    try {
        downloader.install(url, version)
    } catch (e: Exception) {
        view.important("important.installation_failed")
        view.log(e)
        exitAfterDelay(settings.delay, ExitStatus.INSTALLATION_FAILED)
        return false
    }
    if (!settings.preserveDistributive) {
        try {
            view.info("info.removing_archive")
            Files.delete(downloader.distribution.toPath())
        } catch (e: IOException) {
            view.warn("warn.could_not_delete_archive")
            view.log(e)
        }
    }
    view.info("info.success")

    return true
}

private fun askUser(question: String): Boolean {
    view.blank()
    val answer = view.input(question, listOf("Y", "N"))
    view.blank()
    return answer.isNotEmpty() && answer[0].toLowerCase() == 'y'
}

private fun enableAutorun(launcherExecFilePath: Path) {
    view.info("info.setting_autorun_up")
    setAutorunUp(launcherExecFilePath)
}

private fun disableAutorun(launcherExecFilePath: Path) {
    view.info("info.resetting_autorun")
    resetAutorun(launcherExecFilePath)
}

private fun exitAfterDelay(quitDelay: Number, status: ExitStatus = ExitStatus.OK) {
    view.important("important.window_will_be_closed", quitDelay)
    Timer().schedule(quitDelay.toLong() * 1000) {
        exitProcess(status.code)
    }
}
