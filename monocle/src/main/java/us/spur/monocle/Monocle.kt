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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class Monocle private constructor(context: Context, rClass: Class<*>? = null) {

    private val contextRef = WeakReference(context)
    private val v = "0.0.1"
    private val t = "android"
    private val rClass = rClass

    companion object {
        @Volatile
        private var instance: Monocle? = null

        private lateinit var config: MonocleConfig

        fun setup(config: MonocleConfig, context: Context, rClass: Class<*>? = null) {
            Companion.config = config
            instance = Monocle(context, rClass)
        }

        fun getInstance(): Monocle {
            return instance ?: throw IllegalStateException("us.spur.monocle.Monocle must be setup before using")
        }
    }

    // Asynchronous device identifier function, accessible throughout Monocle
    suspend fun getDeviceIdentifier(): String {
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
                val appInfoPlugin = AppInfoPlugin(context, v, t, deviceID, config.token, MonoclePluginConfig("p/ai", 1), rClass)

                // Await plugin results
                val deviceInfo = async { deviceInfoPlugin.trigger() }.await()
                val location = async { locationPlugin.trigger() }.await()
                val dnsResult = async { dnsPlugin.trigger() }.await()
                val appInfo = async { appInfoPlugin.trigger() }.await()

                Log.d("Monocle", "location: $location")
                Log.d("Monocle", "deviceInfo: $deviceInfo")
                Log.d("Monocle", "dnsResult: $dnsResult")
                Log.d("Monocle", "appInfo: $appInfo")

                // Add plugin results to a list
                val pluginResults = listOf(deviceInfo, location, dnsResult, appInfo)

                // Create BundlePostData with the plugin results
                val bundlePostData = BundlePostData(h = pluginResults)

                // Serialize BundlePostData to JSON string
                val jsonString = Json.encodeToString(bundlePostData)

                // Post the JSON string to the backend
                val bundleResponse = BundlePoster(
                    v = "0.0.1",
                    t = "android",
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

data class MonocleConfig(
    val token: String
)