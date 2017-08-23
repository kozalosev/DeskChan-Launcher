package info.deskchan.installer

import java.net.URL
import java.nio.file.Path


private const val REQUEST_URL = "https://api.github.com/repos/DeskChan/DeskChan/releases"
private const val DEFAULT_DOWNLOAD_URL = "https://deskchan.info/deskchan.zip"


class VersionResolver(localPath: Path, manifestFilename: String) {

    private val latestReleaseInfo = RemoteVersionRequester(URL(REQUEST_URL)).tryGetReleaseInfo()
    private val installedReleaseInfo = InstalledVersionRequester(localPath, manifestFilename).tryGetReleaseInfo()

    val isUpdateNeeded: Boolean
        get() = when {
            latestReleaseInfo == null -> false
            installedReleaseInfo == null -> true
            latestReleaseInfo.versionObject == null || installedReleaseInfo.versionObject == null -> {
                latestReleaseInfo.version == installedReleaseInfo.version
            }
            else -> {
                val installed = installedReleaseInfo.versionObject
                val latest = latestReleaseInfo.versionObject
                latest.major > installed.major ||
                        latest.minor > installed.minor ||
                        latest.patch > installed.patch ||
                        latest.commitNumber > installed.commitNumber
            }
        }

    val latestVersion = latestReleaseInfo?.versionObject ?: latestReleaseInfo!!.version
    val latestVersionUrl = latestReleaseInfo?.url ?: URL(DEFAULT_DOWNLOAD_URL)

    val installedVersion = installedReleaseInfo?.versionObject ?: installedReleaseInfo?.version

}


data class Version(val major: Int, val minor: Int, val patch: Int, val commitNumber: Int) {

    companion object {
        fun fromString(version: String): Version? {
            val expr = "v([0-9]+)\\.([0-9]+)\\.([0-9]+)-r([0-9]+)".toRegex()
            val matches = expr.matchEntire(version)?.groups

            if (matches != null && matches.size >= 5) {
                return try {
                    val major = matches[1]!!.value.toInt()
                    val minor = matches[2]!!.value.toInt()
                    val patch = matches[3]!!.value.toInt()
                    val commitNumber = matches[4]!!.value.toInt()
                    Version(major, minor, patch, commitNumber)
                } catch (e: Exception) {
                    view.log(e)
                    null
                }

            }
            return null
        }
    }

    override fun toString() = "v$major.$minor.$patch-r$commitNumber"

}


data class Release(val version: String, val url: URL? = null) {
    val versionObject: Version? = Version.fromString(version)
}