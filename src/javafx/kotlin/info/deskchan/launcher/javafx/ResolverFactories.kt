package info.deskchan.launcher.javafx

import info.deskchan.launcher.*
import info.deskchan.launcher.versioning.*
import java.nio.file.Path


private fun getReleaseFromGitHub(repository: Repository): Release? = try {
    GitHubVersionRequester(repository).getReleaseInfo()
} catch (e: VersionInfoException) {
    when (e) {
        is VersionInfoNotFoundException -> warn("warn.could_not_reach_github")
        is InvalidVersionInfoException -> warn("warn.invalid_json")
    }
    null
}

fun getDeskChanVersionResolver(path: Path): VersionResolver {
    val latestVersionInfo = getReleaseFromGitHub(APPLICATION_REPOSITORY)
    val installedVersionInfo = try {
        InstalledVersionRequester(path, MANIFEST_FILENAME).getReleaseInfo()
    } catch (e: VersionInfoException) {
        when (e) {
            is VersionInfoNotFoundException -> log("info.manifest_not_found")
            is InvalidVersionInfoException -> warn("warn.invalid_manifest")
        }
        null
    }
    return ManifestVersionResolver(latestVersionInfo, installedVersionInfo)
}


fun getLauncherVersionResolver() = ManifestVersionResolver(getReleaseFromGitHub(LAUNCHER_REPOSITORY), Release(env.version))
