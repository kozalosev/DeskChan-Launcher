package info.deskchan.launcher.cli

import info.deskchan.launcher.util.CallbackByteChannel
import info.deskchan.launcher.ZipInstallerEventListeners
import info.deskchan.launcher.util.ByteSizeStringConverter
import java.nio.file.Path


fun getEventListeners(progressBarLength: Int = 20) = object : ZipInstallerEventListeners {

    override fun distributionFound(path: Path) = view.info("info.distribution_found", path.toString())

    override fun partFound(size: Long) = view.info("info.part_of_archive_found", ByteSizeStringConverter(size).toString())

    override fun noSize(size: Long) = view.warn("warn.could_not_determine_size")

    override fun beforeDownloading() = view.blank()

    override fun downloadingProgress(channel: CallbackByteChannel, progress: Double, size: Long) {
        if (progress < 0) {
            view.update(ByteSizeStringConverter(channel.readSoFar).toString())
            return
        }

        val downloaded = Math.round(progress * progressBarLength / 100).toInt()
        val notDownloadedYet = progressBarLength - downloaded
        val progressBar = "=".repeat(downloaded) + "-".repeat(notDownloadedYet)

        val downloadedChunkSize = ByteSizeStringConverter(channel.readSoFar).toString()
        val totalSize = ByteSizeStringConverter(size).toString()

        view.update("[%s] %6.2f%% (%s / %s)".format(progressBar, progress, downloadedChunkSize, totalSize))
    }

    override fun afterDownloading() = view.blank(2)

    override fun manifestGenerating() = view.info("info.generating_manifest")

    override fun manifestGeneratingFailed(e: Throwable) {
        view.warn("warn.could_not_generate_manifest")
        view.log(e)
    }

    override fun switchedToTempDir(newPath: Path) = view.warn("warn.switched_to_temp_dir", newPath.toString())

    override fun extraction() = view.info("info.extracting_files")

    override fun extractionFailed(e: Throwable) {
        view.warn("warn.extracting_error")
        view.log(e)
    }

    override fun installationFailed(e: Throwable) {
        view.warn("warn.installation_failed")
        view.log(e)
    }
}
