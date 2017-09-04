package info.deskchan.launcher.versioning

import info.deskchan.launcher.InvalidVersionInfoException
import info.deskchan.launcher.VersionInfoNotFoundException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Path


interface VersionRequester {
    fun getReleaseInfo(): Release
}


class RemoteJsonRequester(private val remoteUrl: URL) {

    private fun requestJson(): JSONArray {
        val stream = remoteUrl.openConnection().getInputStream()
        val response = BufferedReader(InputStreamReader(stream))
        val text = response.readText()

        val tokener = JSONTokener(text)
        return JSONArray(tokener)
    }

    fun parse(parser: (json: JSONArray) -> Release) = try {
        val json = requestJson()
        parser(json)
    } catch (e: JSONException) {
        throw InvalidVersionInfoException()
    } catch (e: IOException) {
        throw VersionInfoNotFoundException()
    }

}


class GitHubVersionRequester(repo: Repository) : VersionRequester {

    private val requester = RemoteJsonRequester(repo.releasesUrl)

    private fun parseLatestVersion(json: JSONArray): Release {
        val latest = json.getJSONObject(0)
        val version = latest.getString("name")
        val archive = latest.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
        return Release(version, URL(archive))
    }

    override fun getReleaseInfo() = requester.parse(this::parseLatestVersion)
}


class InstalledVersionRequester(private val rootDirPath: Path, private val manifestFilename: String) : VersionRequester {

    private fun readInstalledVersionInfo(): JSONObject {
        val manifestFile = rootDirPath.resolve(manifestFilename).toFile()
        if (!manifestFile.canRead()) {
            throw VersionInfoNotFoundException()
        }

        val text = manifestFile.readText()
        val tokener = JSONTokener(text)
        return JSONObject(tokener)
    }

    private fun parseInstalledVersionInfo(json: JSONObject): Release {
        val version = json.getString("version")
        return Release(version)
    }

    override fun getReleaseInfo() = try {
        val json = readInstalledVersionInfo()
        parseInstalledVersionInfo(json)
    } catch (e: JSONException) {
        throw InvalidVersionInfoException()
    }

}


data class Release(val version: String, val url: URL? = null) {
    val versionObject: Version? = parseVersion(version)
}
