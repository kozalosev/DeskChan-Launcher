package info.deskchan.launcher.javafx

import info.deskchan.launcher.LocalizationFactory


internal object getLocalization {
    operator fun invoke() = LocalizationFactory(javaClass.`package`.name).getLocalization()
}
internal fun String.localize(vararg vars: Any) = localization.getString(this).format(*vars)
