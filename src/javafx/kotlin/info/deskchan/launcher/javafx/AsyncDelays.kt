package info.deskchan.launcher.javafx

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.util.Duration
import javafx.event.EventHandler
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Callable


fun runAfter(seconds: Int, func: () -> Unit) {
    val keyFrame = KeyFrame(Duration.seconds(seconds.toDouble()), EventHandler { _ -> func() })
    Timeline(keyFrame).play()
}

fun <T> runTaskWithinAtLeast(seconds: Int, working_func: () -> T, callback: (T) -> Unit) {
    val future = Executors.newSingleThreadExecutor().submit(Callable<T>(working_func))
    waitForCompletion(future, seconds, callback)
}

fun <T> waitForCompletion(future: Future<T>, seconds: Int, callback: (T) -> Unit) {
    if (!(future.isDone || future.isCancelled)) {
        runAfter(seconds) { waitForCompletion(future, seconds, callback) }
    } else {
        callback(future.get())
    }
}
