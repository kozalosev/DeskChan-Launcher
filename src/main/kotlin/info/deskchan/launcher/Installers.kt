package info.deskchan.launcher

import info.deskchan.launcher.util.CallbackByteChannel
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


interface InstallerEventListeners {
    fun beforeDownloading() {}
    fun downloadingProgress(channel: CallbackByteChannel, progress: Double, size: Long)
    fun afterDownloading() {}

    fun partFound(size: Long) {}
    fun noSize(size: Long) {}

    fun manifestGenerating() {}
    fun manifestGeneratingFailed(e: Throwable) {}
}


abstract class BaseInstaller(private val dir: Path, private val manifestFilename: String) : Installer {

    init {
        if (!Files.isDirectory(dir)) {
            if (Files.notExists(dir)) {
                if (!dir.toFile().mkdir()) {
                    throw CouldNotCreateDirectoryException()
                }
            } else {
                throw NotDirectoryException()
            }
        }
    }

    @Throws(IOException::class)
    open protected fun generateVersionManifest(version: Serializable) {
        val json = JSONObject()
        json.put("version", version)
        val manifest = dir.resolve(manifestFilename).toFile()
        val writer = manifest.outputStream().bufferedWriter()
        writer.write(json.toString(4))
        writer.flush()
    }

}


abstract class InternetInstaller(dir: Path, manifestFilename: String, private val listeners: InstallerEventListeners)
    : BaseInstaller(dir, manifestFilename) {

    @Throws(IOException::class)
    protected fun download(url: URL, file: File, resumeIfPossible: Boolean = true) {
        val connection = url.openConnection()

        val useResuming = resumeIfPossible && file.exists()
        if (useResuming) {
            listeners.partFound(file.length())
            connection.setRequestProperty("Range", "bytes=${file.length()}-")
        }

        val size: Long = connection.contentLength.toLong()
        if (size <= 0) {
            listeners.noSize(size)
        }

        listeners.beforeDownloading()
        val fileStream = FileOutputStream(file, useResuming)
        fileStream.use {
            val urlStream = connection.getInputStream()
            urlStream.use {
                val urlChannel = CallbackByteChannel(Channels.newChannel(urlStream), size) { channel, progress ->
                    listeners.downloadingProgress(channel, progress, size)
                }
                urlChannel.use {
                    val fileChannel = fileStream.channel
                    fileChannel.use {
                        val position = if (useResuming) fileChannel.size() else 0
                        fileChannel.position(position)
                        fileChannel.transferFrom(urlChannel, position, Long.MAX_VALUE)
                    }
                }
            }
        }
        listeners.afterDownloading()
    }

    override fun generateVersionManifest(version: Serializable) {
        listeners.manifestGenerating()
        try {
            super.generateVersionManifest(version)
        } catch (e: IOException) {
            listeners.manifestGeneratingFailed(e)
        }
    }

}


interface ZipInstallerEventListeners : InstallerEventListeners {
    fun extraction() {}
    fun extractionFailed(e: Throwable) {}

    fun deletion() {}
    fun deletionFailed(e: Throwable) {}
}


class ZipInstaller(private val dir: Path, manifestFilename: String, private val listeners: ZipInstallerEventListeners)
    : InternetInstaller(dir, manifestFilename, listeners = listeners) {

    private fun extract(zip: File) {
        listeners.extraction()

        val file = ZipFile(zip)
        file.extractAll(dir.toString())
    }

    private fun delete(zip: File) {
        listeners.deletion()

        try {
            Files.delete(zip.toPath())
        } catch (e: IOException) {
            listeners.deletionFailed(e)
        }
    }

    @Throws(IOException::class, ZipException::class)
    override fun install(url: URL, version: String) {
        val zip = env.rootDirPath.resolve("$version.zip").toFile()
        try {
            download(url, zip)
            extract(zip)
        } catch (e: ZipException) {
            listeners.extractionFailed(e)
            download(url, zip, false)
            extract(zip)
        }
        delete(zip)
        generateVersionManifest(version)
    }

}
