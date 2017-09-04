package info.deskchan.launcher.cli

import info.deskchan.launcher.util.CallbackByteChannel
import info.deskchan.launcher.ZipInstallerEventListeners


fun getEventListeners(progressBarLength: Int = 20) = object : ZipInstallerEventListeners {

    override fun partFound(size: Long) = view.info("info.part_of_archive_found", size.toHumanReadableString())

    override fun noSize(size: Long) = view.warn("warn.could_not_determine_size")

    override fun beforeDownloading() = view.blank()

    override fun downloadingProgress(channel: CallbackByteChannel, progress: Double, size: Long) {
        if (progress < 0) {
            view.update(channel.readSoFar.toHumanReadableString())
            return
        }

        val downloaded = Math.round(progress * progressBarLength / 100).toInt()
        val notDownloadedYet = progressBarLength - downloaded
        val progressBar = "=".repeat(downloaded) + "-".repeat(notDownloadedYet)

        val downloadedChunkSize = channel.readSoFar.toHumanReadableString()
        val totalSize = size.toHumanReadableString()

        view.update("[%s] %6.2f%% (%s / %s)".format(progressBar, progress, downloadedChunkSize, totalSize))
    }

    override fun afterDownloading() = view.blank(2)

    override fun manifestGenerating() = view.info("info.generating_manifest")

    override fun manifestGeneratingFailed(e: Throwable) {
        view.warn("warn.could_not_generate_manifest")
        view.log(e)
    }

    override fun extraction() = view.info("info.extracting_files")

    override fun extractionFailed(e: Throwable) {
        view.warn("warn.extracting_error")
        view.log(e)
    }

    override fun deletion() = view.info("info.removing_archive")

    override fun deletionFailed(e: Throwable) {
        view.warn("warn.could_not_delete_archive")
        view.log(e)
    }

}


// http://programming.guide/java/formatting-byte-size-to-human-readable-format.html
private fun Long.toHumanReadableString(si: Boolean = false): String {
    val unit = if (si) 1000 else 1024
    if (this < unit) return "$this B"
    val exp = (Math.log(this.toDouble()) / Math.log(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return "%.1f %sB".format(this / Math.pow(unit.toDouble(), exp.toDouble()), pre)
}
