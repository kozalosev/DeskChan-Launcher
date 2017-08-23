package info.deskchan.installer

import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path


interface Installer {
    fun install(url: URL, version: String)
}


abstract class InternetInstaller(private val dir: Path, private val manifestFilename: String, private val progressBarLength: Int = 20) : Installer {

    abstract override fun install(url: URL, version: String)

    @Throws(IOException::class)
    protected fun download(url: URL, file: File, resumeIfPossible: Boolean = true) {
        val connection = url.openConnection()

        val useResuming = resumeIfPossible && file.exists()
        if (useResuming) {
            view.info("info.part_of_archive_found", file.length().toHumanReadableString())
            connection.setRequestProperty("Range", "bytes=${file.length()}-")
        }

        val size: Long = connection.contentLength.toLong()
        if (size <= 0) {
            view.warn("warn.could_not_determine_size")
        }

        val fileStream = FileOutputStream(file, useResuming)
        fileStream.use {
            val urlStream = connection.getInputStream()
            urlStream.use {
                val urlChannel = CallbackByteChannel(Channels.newChannel(urlStream), size) { channel, progress ->
                    if (progress < 0) {
                        view.raw(channel.readSoFar.toHumanReadableString() + '\r')
                        return@CallbackByteChannel
                    }

                    val downloaded = Math.round(progress * progressBarLength / 100).toInt()
                    val notDownloadedYet = progressBarLength - downloaded
                    val progressBar = "=".repeat(downloaded) + "-".repeat(notDownloadedYet)

                    val downloadedChunkSize = channel.readSoFar.toHumanReadableString()
                    val totalSize = size.toHumanReadableString()

                    view.update("[%s] %6.2f%% (%s / %s)".format(progressBar, progress, downloadedChunkSize, totalSize))
                }
                urlChannel.use {
                    val fileChannel = fileStream.channel
                    fileChannel.use {
                        view.blank()
                        val position = if (useResuming) fileChannel.size() else 0
                        fileChannel.position(position)
                        fileChannel.transferFrom(urlChannel, position, Long.MAX_VALUE)
                        view.blank(2)
                    }
                }
            }
        }
    }

    protected fun generateVersionManifest(version: Serializable) {
        view.info("info.generating_manifest")

        val json = JSONObject()
        json.put("version", version)
        val manifest = dir.resolve(manifestFilename).toFile()
        val writer = manifest.outputStream().bufferedWriter()
        try {
            writer.write(json.toString(4))
            writer.flush()
        } catch (e: IOException) {
            view.warn("warn.could_not_generate_manifest")
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

}


class ZipInstaller(private val dir: Path, manifestFilename: String) : InternetInstaller(dir, manifestFilename) {

    private fun extract(zip: File) {
        view.info("info.extracting_files")

        val file = ZipFile(zip)
        file.extractAll(dir.toString())
    }

    private fun delete(zip: File) {
        view.info("info.removing_archive")

        try {
            Files.delete(zip.toPath())
        } catch (e: IOException) {
            view.warn("warn.could_not_delete_archive")
            view.log(e)
        }
    }

    @Throws(IOException::class, ZipException::class)
    override fun install(url: URL, version: String) {
        view.info("info.install_version", version)

        val zip = env.rootDirPath.resolve("$version.zip").toFile()
        try {
            download(url, zip)
            extract(zip)
        } catch (e: ZipException) {
            view.warn("warn.extracting_error")
            download(url, zip, false)
            extract(zip)
        }
        delete(zip)
        generateVersionManifest(version)

        view.info("info.success")
    }

}
