package info.deskchan.launcher.versioning

import java.net.URL


data class Repository(val user: String, val repository: String) {
    val releasesUrl by lazy { URL("https://api.github.com/repos/$user/$repository/releases") }
}


interface VersionResolver {
    val isUpdateNeeded: Boolean

    val latestVersion: Any?
    val latestVersionUrl: URL?

    val installedVersion: Any?
}


abstract class BaseVersionResolver(latestRelease: Release?) : VersionResolver {
    override val latestVersion = latestRelease?.versionObject ?: latestRelease?.version
    override val latestVersionUrl = latestRelease?.url
}


class ManifestVersionResolver(latestRelease: Release?, installedRelease: Release?) : BaseVersionResolver(latestRelease) {

    override val isUpdateNeeded: Boolean by lazy {
        when {
            latestRelease == null -> false
            installedRelease == null -> true
            latestRelease.versionObject == null || installedRelease.versionObject == null -> {
                latestRelease.version == installedRelease.version
            }
            else -> latestRelease.versionObject > installedRelease.versionObject
        }
    }

    override val installedVersion = installedRelease?.versionObject ?: installedRelease?.version

}
