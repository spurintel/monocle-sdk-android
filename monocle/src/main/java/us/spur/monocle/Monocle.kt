package us.spur.monocle

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.util.*

object MonoclePluginOptions {
    const val DNS = 1 shl 0       // 1
    const val DEVICE_INFO = 1 shl 1  // 2
    const val LOCATION = 1 shl 2     // 4
    const val ALL = DNS or DEVICE_INFO or LOCATION  // 7
}

data class MonocleConfig(
    val token: String,
    val enabledPlugins: Int = MonoclePluginOptions.ALL,
    val decryptionToken: String? = null
)

class Monocle private constructor(context: Context) {

    private val contextRef = WeakReference(context)
    private val v = "0.0.1"
    private val t = "android"

    companion object {
        @Volatile
        private var instance: Monocle? = null

        private lateinit var config: MonocleConfig

        fun setup(config: MonocleConfig, context: Context) {
            Companion.config = config
            instance = Monocle(context)
        }

        fun getInstance(): Monocle {
            return instance ?: throw IllegalStateException("us.spur.monocle.Monocle must be setup before using")
        }
    }

    // Asynchronous device identifier function, accessible throughout Monocle
    private suspend fun getDeviceIdentifier(): String {
        val context = contextRef.get() ?: throw IllegalStateException("Context is no longer available")
        return withContext(Dispatchers.IO) {
            // Try to get a hardware identifier (ANDROID_ID)
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrEmpty()) {
                return@withContext androidId
            }

            // Try to get the Advertising ID
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                if (!adInfo.isLimitAdTrackingEnabled) {
                    return@withContext adInfo.id ?: UUID.randomUUID().toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fall back to a randomly generated UUID
            UUID.randomUUID().toString()
        }
    }

    // Define BundlePostData with list of MonoclePluginResponse objects
    @Serializable
    data class BundlePostData(
        val h: List<MonoclePluginResponse>
    )

    suspend fun assess(): Result<AssessmentResponse> {
        val context = contextRef.get() ?: return Result.failure(IllegalStateException("Context is no longer available"))
        val pluginsList = mutableListOf<MonoclePlugin>()
        val pluginsResponses = mutableListOf<MonoclePluginResponse>()
        return coroutineScope {
            try {
                // Concurrently gather data from all plugins
                val deviceID = getDeviceIdentifier()
                val deviceInfoPlugin =
                    DeviceInfoPlugin(context, v, t, deviceID, config.token, MonoclePluginConfig("p/di", 1))
                val locationPlugin =
                    LocationPlugin(context, v, t, deviceID, config.token, MonoclePluginConfig("p/li", 1))
                val dnsPlugin =
                    DnsResolverPlugin(OkHttpClient(), v, t, deviceID, config.token, MonoclePluginConfig("p/dr", 1))

                if (config.enabledPlugins and MonoclePluginOptions.DNS != 0) {
                    pluginsList.add(dnsPlugin)
                }
                if (config.enabledPlugins and MonoclePluginOptions.DEVICE_INFO != 0) {
                    pluginsList.add(deviceInfoPlugin)
                }
                if (config.enabledPlugins and MonoclePluginOptions.LOCATION != 0) {
                    pluginsList.add(locationPlugin)
                }

                for (plugin in pluginsList) {
                    val response = async { plugin.trigger() }.await()
                    pluginsResponses.add(response)
                    Log.d("Monocle", "response: $response")
                }


                // Create BundlePostData with the plugin results
                val bundlePostData = BundlePostData(h = pluginsResponses)

                // Serialize BundlePostData to JSON string
                val jsonString = Json.encodeToString(bundlePostData)

                // Post the JSON string to the backend
                val bundleResponse = BundlePoster(
                    v = v,
                    t = t,
                    s = deviceID,
                    tk = config.token
                ).postBundle(jsonString)

                // Create and return the AssessmentResponse
                bundleResponse.fold(
                    onSuccess = { responseData ->
                        // decode responseData into AssessmentResponse
                        val assessmentResponse = Json.decodeFromString<AssessmentResponse>(responseData)
                        Result.success(assessmentResponse)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
