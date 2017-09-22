package info.deskchan.launcher.javafx

import info.deskchan.launcher.ExitStatus
import info.deskchan.launcher.exitProcess
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.*
import javafx.scene.control.ButtonType.*


private fun showAlert(message: String, type: Alert.AlertType, vararg vars: Any) = Platform.runLater {
    val alert = Alert(type, message.localize(*vars), OK)
    alert.show()
}

internal fun warn(message: String, vararg vars: Any) = showAlert(message, WARNING, *vars)
internal fun error(message: String, vararg vars: Any) = showAlert(message, ERROR, *vars)

internal fun fatalError(message: String, status: ExitStatus, vararg vars: Any): Nothing {
    Platform.runLater {
        val alert = Alert(ERROR, message.localize(*vars), OK)
        alert.showAndWait()
        exitProcess(status)
    }
    while (true) {}
}

internal fun confirm(message: String, vararg vars: Any): Boolean {
    val alert = Alert(CONFIRMATION, message.localize(*vars), YES, NO)
    val result = alert.showAndWait().get()
    return result == YES
}
