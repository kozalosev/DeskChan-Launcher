package info.deskchan.launcher.javafx

import info.deskchan.launcher.ExitStatus
import info.deskchan.launcher.env
import info.deskchan.launcher.exitProcess
import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.util.Callback
import java.nio.file.Paths


abstract class BaseController {
    @FXML private fun quit() {
        exitProcess(ExitStatus.OK)
    }
}


class SplashScreenController {

    private val _applicationName: ReadOnlyStringProperty = SimpleStringProperty(application.applicationName)

    val applicationName: String
        get() = _applicationName.value
    fun applicationNameProperty() = _applicationName


    @FXML lateinit var splashScreenStatus: Label

    @FXML private fun initialize() {
        splashScreenStatus.textProperty().bind(application.splashScreenStatus)
    }

}


class SettingsController : BaseController() {

    @FXML lateinit var installationPathTextField: TextField
    @FXML lateinit var autorunEnabledCheckBox: CheckBox

    @FXML private fun moveToInstallation() {
        val dir = Paths.get(installationPathTextField.text).toFile()
        if (!dir.exists()) {
            dir.mkdirs()
        }
        if (!dir.isDirectory) {
            error("error.wrong_path")
            return
        }
        if (dir.path != env.rootDirPath.toString()
                && dir.list().isNotEmpty()
                && !confirm("input.install_to_existing_dir")) {
            return
        }
        application.moveToInstallationScene()
    }

    @FXML private fun showChooser() {
        val chooser = DirectoryChooser()
        chooser.title = "chooser.title".localize()
        chooser.initialDirectory = env.rootDirPath.toFile()
        chooser.showDialog(application.stage)?.let {
            installationPathTextField.text = it.toString()
        }
    }

    @FXML private fun initialize() {
        installationPathTextField.textProperty().bindBidirectional(application.installationPath)
        autorunEnabledCheckBox.selectedProperty().bindBidirectional(application.autorunEnabled)
    }

}


class InstallationController : BaseController() {

    var progress: Double?
        get() = application.progress.value
        set(value) {
            application.progress.value = value
        }
    fun progressProperty() = application.progress

    var logList: ObservableList<String>?
        get() = application.installationLog.value
        set(value) {
            application.installationLog.value = value
        }
    fun logListProperty() = application.installationLog


    @FXML lateinit var logListView: ListView<*>

    @FXML private fun initialize() {
        logListView.cellFactory = Callback { list -> object : ListCell<String>() {
            init {
                val textView = Text()
                textView.wrappingWidthProperty().bind(list.widthProperty().subtract(15))
                textProperty().addListener { _, _, newValue ->
                    if (newValue.startsWith("! ")) {
                        textView.style = "-fx-fill: red;"
                        textView.text = newValue.drop(2)
                    } else {
                        textView.style = ""
                        textView.text = newValue
                    }
                }
                graphic = textView
                prefWidth = 0.0
            }

            override fun updateItem(item: String?, empty: Boolean) {
                super.updateItem(item, empty)
                text = item ?: ""
            }
        }}
    }

}
