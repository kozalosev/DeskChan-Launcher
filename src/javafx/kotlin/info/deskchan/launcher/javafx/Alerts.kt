package info.deskchan.launcher.javafx

import info.deskchan.launcher.ExitStatus
import info.deskchan.launcher.exitProcess
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.*
import javafx.scene.control.ButtonType.*


private fun showAlert(message: String, type: Alert.AlertType, vararg vars: Any) = Platform.runLater {
    val localizedMessage = message.localize(*vars)
    log(localizedMessage)
    val alert = Alert(type, localizedMessage, OK)
    alert.show()
}

internal fun warn(message: String, vararg vars: Any) = showAlert(message, WARNING, *vars)
internal fun error(message: String, vararg vars: Any) = showAlert(message, ERROR, *vars)

internal fun fatalError(message: String, status: ExitStatus, vararg vars: Any): Nothing {
    val localizedMessage = message.localize(*vars)
    log(localizedMessage)
    Platform.runLater {
        val alert = Alert(ERROR, localizedMessage, OK)
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
