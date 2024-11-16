package us.spur.monocle

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class DeviceInformation(
    val name: String,
    val systemName: String,
    val systemVersion: String,
    val model: String,
    val identifierForVendor: String,
    val isBatteryMonitoringEnabled: Boolean,
    val batteryLevel: Float,
    val screenSize: String,
    val brightness: Float,
    val currentLocale: String,
    val timeZone: String,
    val networkType: String,
    val installedApps: List<String> = emptyList(),
)

class DeviceInfoPlugin(
    context: Context,
    v: String,
    t: String,
    private val s: String,
    tk: String,
    config: MonoclePluginConfig
) : MonoclePlugin(v, t, s, tk, config) {

    private val contextRef = context.applicationContext

    override suspend fun trigger(): MonoclePluginResponse {
        val start = getCurrentDateTime()
        val response = MonoclePluginResponse(pid = config.pid, version = config.version, start = start)
        try {
            val data = gatherDeviceInformation()
            val serializedData = serialize(data)
            response.data = serializedData
        } catch (e: Exception) {
            response.error = e.localizedMessage
            e.printStackTrace()
        }
        response.end = getCurrentDateTime()
        return response
    }

    private suspend fun gatherDeviceInformation(): DeviceInformation {
        val s = this.s
        return withContext(Dispatchers.IO) {
            val networkType = getNetworkType()

            val batteryManager = contextRef.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val windowManager = contextRef.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat()
            val brightness =
                Settings.System.getInt(contextRef.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
            val locale = Locale.getDefault().toString()

            val installedApps = getInstalledApps()

            val fields = Build.VERSION_CODES::class.java.fields
            var codeName = "UNKNOWN"
            fields.filter { it.getInt(Build.VERSION_CODES::class) == Build.VERSION.SDK_INT }
                .forEach { codeName = it.name }

            DeviceInformation(
                name = Build.DEVICE,
                systemName = codeName,
                systemVersion = Build.VERSION.RELEASE,
                model = Build.MODEL,
                identifierForVendor = s,
                isBatteryMonitoringEnabled = true,
                batteryLevel = batteryLevel,
                screenSize = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
                brightness = brightness,
                currentLocale = locale,
                timeZone = TimeZone.getDefault().id,
                networkType = networkType,
                installedApps = installedApps,
            )
        }
    }

    private suspend fun getNetworkType(): String {
        return withContext(Dispatchers.IO) {
            val connectivityManager = contextRef.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (ContextCompat.checkSelfPermission(
                    contextRef,
                    android.Manifest.permission.ACCESS_NETWORK_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext "No Permission"
            }

            val network = connectivityManager.activeNetwork ?: return@withContext "No Connection"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext "No Connection"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                else -> "Unknown"
            }
        }
    }

    private fun getInstalledApps(): List<String> {
        return try {
            val packageManager = contextRef.packageManager
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            apps.map { it.packageName }
                .filter { packageName ->
                    !(packageName.startsWith("com.google") || packageName.startsWith("kotlin") || packageName.startsWith(
                        "us.spur"
                    ) || packageName.startsWith("android") || packageName.startsWith("com.android") || packageName.startsWith(
                        "javax"
                    ) || packageName.startsWith(contextRef.packageName))
                }
        } catch (e: SecurityException) {
            emptyList()
        }
    }
}