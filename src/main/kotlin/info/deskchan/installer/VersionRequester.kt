package info.deskchan.installer

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Path


private interface VersionRequester {
    fun tryGetReleaseInfo(): Release?
}


internal class RemoteVersionRequester(private val remoteUrl: URL) : VersionRequester {

    private fun requestJsonFromGithub(): JSONArray {
        val stream = remoteUrl.openConnection().getInputStream()
        val response = BufferedReader(InputStreamReader(stream))
        val text = response.readText()

        val tokener = JSONTokener(text)
        return JSONArray(tokener)
    }

    private fun parseLatestVersion(json: JSONArray): Release {
        val latest = json.getJSONObject(0)
        val version = latest.getString("name")
        val archive = latest.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
        return Release(version, URL(archive))
    }

    override fun tryGetReleaseInfo() = try {
        val json = requestJsonFromGithub()
        parseLatestVersion(json)
    } catch (e: JSONException) {
        view.warn("warn.invalid_json")
        view.log(e)
        null
    } catch (e: IOException) {
        view.warn("warn.could_not_reach_github")
        view.log(e)
        null
    }

}


internal class InstalledVersionRequester(private val rootDirPath: Path, private val manifestFilename: String) : VersionRequester {

    private fun readInstalledVersionInfo(): JSONObject? {
        val manifestFile = rootDirPath.resolve(manifestFilename).toFile()
        if (!manifestFile.canRead()) {
            view.info("info.manifest_not_found")
            return null
        }

        val text = manifestFile.readText()
        val tokener = JSONTokener(text)
        return JSONObject(tokener)
    }

    private fun parseInstalledVersionInfo(json: JSONObject): Release? {
        val version = json.getString("version")
        return Release(version)
    }

    override fun tryGetReleaseInfo() = try {
        val json = readInstalledVersionInfo()
        json?.let { parseInstalledVersionInfo(it) }
    } catch (e: JSONException) {
        view.warn("warn.invalid_manifest")
        null
    }

}


data class Release(val version: String, val url: URL? = null) {
    val versionObject: Version? = parseVersion(version)
}
