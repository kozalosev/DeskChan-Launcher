package info.deskchan.launcher.javafx

import info.deskchan.launcher.*
import info.deskchan.launcher.versioning.VersionResolver
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import kotlin.properties.Delegates


var application: JavaFxApplication by Delegates.notNull()
internal val localization = getLocalization()


class JavaFxApplication : Application() {

    internal lateinit var stage: Stage
        private set
    private lateinit var installationScene: Scene
    private lateinit var splashScreenScene: Scene

    private lateinit var deskChanVersionResolver: VersionResolver

    internal val applicationName = "$APPLICATION_NAME Launcher ${env.version}"
    internal val splashScreenStatus: StringProperty = SimpleStringProperty("splash_screen.version_checking".localize())

    internal val installationPath: StringProperty = SimpleStringProperty(env.rootDirPath.toString())
    internal val autorunEnabled = SimpleBooleanProperty(false)

    internal val progress = SimpleDoubleProperty(-1.0)
    internal val installationLog = SimpleListProperty(FXCollections.observableArrayList<String>())

    override fun start(primaryStage: Stage) {
        application = this
        val deskChanDirPath = env.rootDirPath.resolve(APPLICATION_NAME)

        val bundle = ResourceBundle.getBundle("${javaClass.`package`.name}.strings")
        val checkingSceneMarkup: Parent = FXMLLoader.load(javaClass.getResource("SplashScreenScene.fxml"), bundle)
        val settingsSceneMarkup: Parent = FXMLLoader.load(javaClass.getResource("SettingsScene.fxml"), bundle)
        val installationSceneMarkup: Parent = FXMLLoader.load(javaClass.getResource("InstallationScene.fxml"), bundle)

        val icon = Image(javaClass.getResource("logo.png").toString())

        stage = primaryStage
        stage.icons += icon
        installationScene = Scene(installationSceneMarkup)
        splashScreenScene = Scene(checkingSceneMarkup)

        val checkingStage = Stage(StageStyle.UNDECORATED)
        checkingStage.scene = splashScreenScene
        checkingStage.show()

        runTaskWithinAtLeast(SPLASHSCREEN_DELAY, working_func = {
            val launcherVersionResolver = getLauncherVersionResolver()
            val deskChanVersionResolver = getDeskChanVersionResolver(deskChanDirPath)
            Pair(launcherVersionResolver, deskChanVersionResolver)
        }, callback = { pair ->
            val launcherVersionResolver = pair.component1()
            deskChanVersionResolver = pair.component2()

                if (launcherVersionResolver.isUpdateNeeded && confirm("confirm.launcher_update")) {
                hostServices.showDocument(LAUNCHER_REPOSITORY.releasesUrl.toString())
                exitProcess(ExitStatus.OK)
            }

            if (deskChanVersionResolver.isUpdateNeeded) {
                stage.title = applicationName
                stage.scene = if (deskChanVersionResolver.installedVersion == null) {
                    stage.minWidth = 350.0
                    stage.minHeight = 230.0
                    Scene(settingsSceneMarkup)
                } else {
                    stage.minWidth = 200.0
                    stage.minHeight = 150.0
                    installationScene
                }
                checkingStage.close()
                stage.show()
                if (deskChanVersionResolver.installedVersion != null) {
                    installationLog.addAll(
                            "info.installed_version".localize(deskChanVersionResolver.installedVersion.toString(),
                            "info.installation_required".localize()
                    ))
                    startInstallation()
                }
            } else {
                splashScreenStatus.value = "important.launching".localize()
                launchApp(getExecFilePath(deskChanDirPath))
                runAfter(EXIT_DELAY) { exitProcess(ExitStatus.OK) }
            }
        })
    }

    // Used by SettingsController
    internal fun moveToInstallationScene() {
        stage.scene = installationScene
        startInstallation()
    }

    private fun startInstallation() {
        val launcherPath = Paths.get(installationPath.value)
        val applicationPath = launcherPath.resolve(APPLICATION_NAME)
        val successCallback: Installer.() -> Unit = {
            Platform.runLater {
                installationLog.add("info.removing_archive".localize())
                distribution.delete()
                if (launcherPath != env.rootDirPath) {
                    installationLog.add("info.copying_launcher".localize())
                    copyLauncherAndGetExecutable(launcherPath)
                }
                if (autorunEnabled.value) {
                    installationLog.add("info.setting_autorun_up".localize())
                    setAutorunUp(getLauncherExecFilePath(launcherPath))
                }
                installationLog.add("info.going_to_launch".localize())
            }

            runAfter(LAUNCH_DELAY) {
                Platform.runLater {
                    val splashScreenStage = Stage(StageStyle.UNDECORATED)
                    splashScreenStage.scene = splashScreenScene
                    splashScreenStatus.value = "important.launching".localize()
                    stage.hide()
                    splashScreenStage.show()
                    stage.close()
                    launchApp(getExecFilePath(applicationPath))
                    runAfter(EXIT_DELAY) { exitProcess(ExitStatus.OK) }
                }
            }
        }
        val installer: Installer
        try {
            installer = ZipInstaller(applicationPath, MANIFEST_FILENAME, getEventListeners(onSuccess = successCallback))
        } catch (e: IOException) {
            log(e)
            val message = when (e) {
                is NotDirectoryException -> "not_directory"
                is CouldNotCreateDirectoryException -> "could_not_create_directory"
                else -> "invalid_installation_path"
            }
            fatalError("important.$message", ExitStatus.INVALID_INSTALLATION_PATH)
        }
        installer.provideSize(deskChanVersionResolver.latestVersionSize)

        val url = deskChanVersionResolver.latestVersionUrl ?: URL(DEFAULT_DOWNLOAD_URL)
        val version = deskChanVersionResolver.latestVersion.toString()
        installationLog.addAll(
                "info.install_version".localize(version),
                "info.working_directory".localize(env.rootDirPath.toString()),
                "info.installation_directory".localize(launcherPath.toString())
        )
        Executors.newSingleThreadExecutor().execute {
            try {
                installer.install(url, version)
            } catch (e: Exception) {
                log(e)
                fatalError("important.installation_failed", ExitStatus.INSTALLATION_FAILED)
            }
        }
    }

}

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
        error("important.no_launcher_after_copying")
    }
    return launcherExecFilePath
}

private fun launchApp(execFilePath: Path) = try {
    launchApplication(execFilePath)
} catch (e: FileNotFoundException) {
    fatalError("important.could_not_find_executable", ExitStatus.EXECUTABLE_NOT_FOUND, execFilePath.toString())
}
