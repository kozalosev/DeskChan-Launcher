package info.deskchan.installer

import java.net.URL
import java.nio.file.Path


data class Repository(val user: String, val repository: String) {
    val releasesUrl by lazy { URL("https://api.github.com/repos/$user/$repository/releases") }
}


interface VersionResolver {
    val isUpdateNeeded: Boolean

    val latestVersion: Any?
    val latestVersionUrl: URL?

    val installedVersion: Any?
}


abstract class RepositoryVersionResolver(repo: Repository) : VersionResolver {
    protected val latestReleaseInfo = RemoteVersionRequester(repo.releasesUrl).tryGetReleaseInfo()
}


class ManifestVersionResolver(repo: Repository, localPath: Path, manifestFilename: String) : RepositoryVersionResolver(repo) {

    private val installedReleaseInfo = InstalledVersionRequester(localPath, manifestFilename).tryGetReleaseInfo()

    override val isUpdateNeeded: Boolean
        get() = when {
            latestReleaseInfo == null -> false
            installedReleaseInfo == null -> true
            latestReleaseInfo.versionObject == null || installedReleaseInfo.versionObject == null -> {
                latestReleaseInfo.version == installedReleaseInfo.version
            }
            else -> latestReleaseInfo.versionObject > installedReleaseInfo.versionObject
        }

    override val latestVersion = latestReleaseInfo?.versionObject ?: latestReleaseInfo?.version
    override val latestVersionUrl = latestReleaseInfo?.url

    override val installedVersion = installedReleaseInfo?.versionObject ?: installedReleaseInfo?.version

}


class StringVersionResolver(repo: Repository, version: String) : RepositoryVersionResolver(repo) {

    override val isUpdateNeeded: Boolean
        get() = when {
            latestReleaseInfo == null -> false
            latestReleaseInfo.versionObject != null && installedVersion is Version -> latestReleaseInfo.versionObject > installedVersion
            else -> latestReleaseInfo.version == installedVersion.toString()
        }

    override val latestVersion = latestReleaseInfo?.versionObject ?: latestReleaseInfo?.version
    override val latestVersionUrl = latestReleaseInfo?.url

    override val installedVersion = parseVersion(version) ?: version

}
