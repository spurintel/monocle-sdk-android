package us.spur.monocle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import kotlinx.serialization.Serializable
import java.util.*

const val regionalDomain = "verify-use.spur.us"

@Serializable
data class ResolverPluginStub(
    val ok: Boolean,
    val id: String,
    val dns: String
)

class DnsResolverPlugin(
    private val client: OkHttpClient = OkHttpClient(),
    private val v: String,
    private val t: String,
    private val s: String,
    private val tk: String,
    config: MonoclePluginConfig
) : MonoclePlugin(v, t, s, tk, config) {

    override suspend fun trigger(): MonoclePluginResponse {
        val start = getCurrentDateTime()
        val response = MonoclePluginResponse(pid = config.pid, version = config.version, start = start)
        try {
            val data = executeResolver()
            val serializedData = serialize(data)
            response.data = serializedData
        } catch (e: Exception) {
            response.error = e.localizedMessage
            e.printStackTrace()
        }
        response.end = getCurrentDateTime()
        return response
    }

    private suspend fun executeResolver(): ResolverPluginStub {
        val id = UUID.randomUUID().toString().lowercase(Locale.getDefault()).replace("-", "")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("$id.$regionalDomain")
            .addPathSegments("d/p")
            .addQueryParameter("s", id)
            .addQueryParameter("v", v)
            .addQueryParameter("t", t)
            .addQueryParameter("tk", tk)
            .build()
        val client = this.client
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                return@withContext if (response.isSuccessful) {
                    val responseBody = response.body
                    if (responseBody == null) {
                        println("DNS Resolver Plugin Response Body is null")
                        return@withContext ResolverPluginStub(ok = false, id = id, dns = "")
                    }

                    val dns = responseBody.string()

                    response.close()

                    return@withContext ResolverPluginStub(ok = true, id = id, dns = dns)
                } else {
                    response.close()
                    ResolverPluginStub(ok = false, id = id, dns = "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ResolverPluginStub(ok = false, id = id, dns = "")
            }
        }
    }
}