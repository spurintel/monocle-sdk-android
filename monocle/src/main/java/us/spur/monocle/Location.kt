package us.spur.monocle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val locationTimestamp: String?
)

class LocationPlugin(
    context: Context,
    v: String,
    t: String,
    s: String,
    tk: String,
    config: MonoclePluginConfig
) : MonoclePlugin(v, t, s, tk, config) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val context = context

    override suspend fun trigger(): MonoclePluginResponse {
        val start = getCurrentDateTime()
        val response = MonoclePluginResponse(pid = config.pid, version = config.version, start = start)
        try {
            if (hasLocationPermission()) {
                val data = gatherLocationInformation()
                val serializedData = serialize(data)
                response.data = serializedData
            } else {
                response.error = "Location permission not granted"
            }
        } catch (e: Exception) {
            response.error = e.localizedMessage
            e.printStackTrace()
        }
        response.end = getCurrentDateTime()
        return response
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED || coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private suspend fun gatherLocationInformation(): LocationData {
        return withContext(Dispatchers.IO) {
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    val timestamp = rfc3339Formatted(location.time)
                    LocationData(latitude = location.latitude, longitude = location.longitude, locationTimestamp = timestamp)
                } else {
                    LocationData(latitude = 0.0, longitude = 0.0, locationTimestamp = null)
                }
            } catch (e: Exception) {
                LocationData(latitude = 0.0, longitude = 0.0, locationTimestamp = null)
            }
        }
    }

    private fun rfc3339Formatted(time: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(time))
    }
}