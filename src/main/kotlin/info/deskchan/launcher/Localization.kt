package info.deskchan.launcher

import java.util.*


class Localization(val languageTag: String, private val strings: Map<String, String>) {
    fun getString(label: String) = strings.getOrDefault(label, label)
}


class LocalizationFactory(private val packageName: String) {

    fun getLocalization() = getSpecificLocalization(Locale.getDefault())

    fun getSpecificLocalization(locale: Locale): Localization {
        val coreResourcePath = javaClass.`package`.name + ".strings"
        val localResourcePath = "$packageName.strings"
        val coreBundle = ResourceBundle.getBundle(coreResourcePath, locale)
        val map = if (coreResourcePath != localResourcePath) {
            val localBundle = ResourceBundle.getBundle(localResourcePath, locale)
            coreBundle.toMap() + localBundle.toMap()
        } else {
            coreBundle.toMap()
        }
        return Localization(locale.toLanguageTag(), map)
    }

    private fun ResourceBundle.toMap(): Map<String, String> = this.keys.toList().associate {
        val isoStr = this.getString(it)
        val utfStr = String(isoStr.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
        it to utfStr
    }

}
