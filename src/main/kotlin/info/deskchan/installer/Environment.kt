package info.deskchan.installer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class Environment {

    val rootDirPath: Path by lazy {
        val executableDir = Paths.get(javaClass.protectionDomain.codeSource.location.toURI())
        var directoryPath = executableDir.parent
        while (!Files.isDirectory(directoryPath)) {
            directoryPath = directoryPath.parent
        }
        view.info("info.working_directory", directoryPath)
        directoryPath
    }

    private val defaultLocalization = getLocalization()

    fun getString(key: String) = defaultLocalization.getString(key)

    fun getLocalization(locale: Locale = Locale.getDefault()): Localization {
        val bundle = ResourceBundle.getBundle(javaClass.`package`.name + ".strings", locale)
        val map = bundle.keys.toList().associate {
            val isoStr = bundle.getString(it)
            val utfStr = String(isoStr.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
            it to utfStr
        }
        return Localization(map)
    }

}


class Localization(private val strings: Map<String, String>) {
    fun getString(label: String) = strings.getOrDefault(label, label)
}


val onWindows: Boolean
    get() = System.getProperty("os.name").startsWith("Windows")
