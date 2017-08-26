package info.deskchan.installer

import com.github.sarxos.winreg.HKey
import com.github.sarxos.winreg.WindowsRegistry
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.rmi.UnexpectedException


interface AutorunManager {
    fun setAutorunUp()
    fun resetAutorun()
}


fun getAutorunManager(appName: String, execPath: Path) = if (onWindows) {
    WindowsAutorunManager(appName, execPath)
} else {
    UnixAutorunManager(appName, execPath)
}


private fun Path.toQuotedString() = '"' + this.toString() + '"'


class WindowsAutorunManager(private val appName: String, private val execPath: Path) : AutorunManager {

    private val registry = WindowsRegistry.getInstance()
    private val tree = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run"

    override fun setAutorunUp() = registry.writeStringValue(HKey.HKCU, tree, appName, execPath.toQuotedString())

    override fun resetAutorun() = registry.deleteValue(HKey.HKCU, tree, appName)

}


class UnixAutorunManager(appName: String, execPath: Path) : AutorunManager {

    private val fileContent = """[Desktop Entry]
        |Type=Application
        |Exec=${execPath.toQuotedString()}
        |Name=$appName
        """.trimMargin()

    private val filePath = getAutostartDir().resolve("$appName.desktop")

    @Throws(UnexpectedException::class)
    private fun getAutostartDir(): Path {
        val configHome = System.getenv("XDG_CONFIG_HOME")
        if (configHome != null) {
            val path = Paths.get(configHome)
            if (path.toFile().exists()) {
                return path
            }
        }

        val userHome = Paths.get(System.getProperty("user.home"))
        val path = userHome.resolve(".config/autostart")
        if (path.toFile().exists()) {
            return path
        } else {
            throw UnexpectedException("Couldn't find the autostart directory!")
        }
    }

    @Throws(IOException::class)
    override fun setAutorunUp() {
        val file = filePath.toFile()
        if (file.exists()) {
            return
        }

        file.printWriter().use {
            it.println(fileContent)
            if (it.checkError()) {
                throw IOException("Couldn't create an autorun file: $filePath")
            }
        }
    }

    @Throws(IOException::class)
    override fun resetAutorun() {
        if (!filePath.toFile().delete()) {
            throw IOException("Couldn't delete the autorun file: $filePath")
        }
    }

}
