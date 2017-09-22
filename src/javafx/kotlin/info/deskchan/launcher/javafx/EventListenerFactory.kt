package info.deskchan.launcher.javafx

import info.deskchan.launcher.Installer
import info.deskchan.launcher.ZipInstallerEventListeners
import info.deskchan.launcher.util.ByteSizeStringConverter
import info.deskchan.launcher.util.CallbackByteChannel
import javafx.application.Platform
import java.nio.file.Path


fun getEventListeners(onSuccess: Installer.() -> Unit) = object : ZipInstallerEventListeners {

    private val log = application.installationLog

    override fun distributionFound(path: Path) {
        write("info.distribution_found", path.toString())
    }

    override fun partFound(size: Long) {
        write("info.part_of_archive_found", ByteSizeStringConverter(size).toString())
    }

    override fun noSize(size: Long) {
        warn("warn.could_not_determine_size")
    }

    override fun beforeDownloading() {
        write("info.downloading_started")
    }

    override fun downloadingProgress(channel: CallbackByteChannel, progress: Double, size: Long) {
        Platform.runLater {
            application.progress.value = progress / 100
        }
    }

    override fun manifestGenerating() {
        write("info.generating_manifest")
    }

    override fun manifestGeneratingFailed(e: Throwable) {
        log(e)
        warn("warn.could_not_generate_manifest")
    }

    override fun switchedToTempDir(newPath: Path) {
        warn("warn.switched_to_temp_dir", newPath.toString())
    }

    override fun extraction() {
        write("info.extracting_files")
    }

    override fun extractionFailed(e: Throwable) {
        log(e)
        warn("warn.extracting_error")
    }

    override fun installed(installer: Installer) = onSuccess(installer)

    override fun installationFailed(e: Throwable) {
        log(e)
        warn("warn.installation_failed")
    }

    private fun write(pattern: String, vararg vars: Any) = Platform.runLater {
        log.add(pattern.localize(*vars))
    }

    private fun warn(pattern: String, vararg vars: Any) = Platform.runLater {
        log.add("! ${pattern.localize(*vars)}")
    }

}
