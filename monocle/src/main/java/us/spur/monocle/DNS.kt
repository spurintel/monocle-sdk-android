package us.spur.monocle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import kotlinx.serialization.Serializable
import java.util.*

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

    private suspend fun getRegionalDomain(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://js.mcl.io/region")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch regional domain")
            response.body?.string()?.trim() ?: throw Exception("Empty regional domain")
        }
    }

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

        return withContext(Dispatchers.IO) {
            try {
                val regionalHost = getRegionalDomain() // already tokenized host from /region
                val hostAndPort = regionalHost
                    .removePrefix("https://")
                    .removePrefix("http://")
                    .trim()
                    .trimEnd('/')

                val hostParts = hostAndPort.split(":")
                val host = hostParts[0]

                val urlBuilder = HttpUrl.Builder()
                    .scheme("https")
                    .host(host)
                    .addPathSegments("d/p")
                    .addQueryParameter("s", id)
                    .addQueryParameter("v", v)
                    .addQueryParameter("t", t)
                    .addQueryParameter("tk", tk)

                if (hostParts.size == 2) {
                    urlBuilder.port(hostParts[1].toInt())
                }

                val request = Request.Builder().url(urlBuilder.build()).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext ResolverPluginStub(ok = false, id = id, dns = "")
                    }

                    val dns = response.body?.string().orEmpty()
                    if (dns.isEmpty()) {
                        ResolverPluginStub(ok = false, id = id, dns = "")
                    } else {
                        ResolverPluginStub(ok = true, id = id, dns = dns)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ResolverPluginStub(ok = false, id = id, dns = "")
            }
        }
    }
}