package us.spur.monocle

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class MonoclePluginConfig(
    val pid: String,
    var version: Int,
)

@Serializable
data class MonoclePluginResponse(
    val pid: String,
    val version: Int,
    val start: String,
    var end: String? = null,
    var data: String? = null,
    var error: String? = null
)

open class MonoclePlugin(
    private val v: String,
    private val t: String,
    private val s: String,
    private val tk: String,
    val config: MonoclePluginConfig
) {
    val pid: String = config.pid
    private val version: Int = config.version

    open suspend fun trigger(): MonoclePluginResponse {
        val start = getCurrentDateTime()
        val response = MonoclePluginResponse(pid, version, start)
        try {
            response.data = serialize(response)
        } catch (e: Exception) {
            response.error = e.localizedMessage
        }
        response.end = getCurrentDateTime()

        return response
    }

    inline fun <reified T> serialize(data: T): String {
        return Json.encodeToString(data)
    }

    fun getCurrentDateTime(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }
}