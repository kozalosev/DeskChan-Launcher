package info.deskchan.launcher.cli

import info.deskchan.launcher.*
import info.deskchan.launcher.versioning.*
import java.nio.file.Path


private fun getReleaseFromGitHub(repository: Repository): Release? = try {
    GitHubVersionRequester(repository).getReleaseInfo()
} catch (e: VersionInfoException) {
    when (e) {
        is VersionInfoNotFoundException -> view.warn("warn.could_not_reach_github")
        is InvalidVersionInfoException -> view.warn("warn.invalid_json")
    }
    null
}

internal fun getDeskChanVersionResolver(deskChanDirPath: Path): VersionResolver {
    val latestVersionInfo = getReleaseFromGitHub(APPLICATION_REPOSITORY)
    val installedVersionInfo = try {
        InstalledVersionRequester(deskChanDirPath, MANIFEST_FILENAME).getReleaseInfo()
    } catch (e: VersionInfoException) {
        when (e) {
            is VersionInfoNotFoundException -> view.info("info.manifest_not_found")
            is InvalidVersionInfoException -> view.warn("warn.invalid_manifest")
        }
        null
    }
    return ManifestVersionResolver(latestVersionInfo, installedVersionInfo)
}


internal fun getLauncherVersionResolver() = ManifestVersionResolver(getReleaseFromGitHub(LAUNCHER_REPOSITORY), Release(env.version))
