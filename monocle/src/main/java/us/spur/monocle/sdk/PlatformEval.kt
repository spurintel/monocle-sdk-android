package us.spur.monocle.sdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.Locale
import android.provider.Settings

class PlatformEval {

    fun getPlatformEval(context: Context): JSONObject {
        val pe = JSONObject()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        val currentDateTime = Date()
        val formattedDateTime = dateFormat.format(currentDateTime)
        pe.put("timestamp_device", formattedDateTime)
        pe.put("brand", Build.BRAND)
        pe.put("manufacturer", Build.MANUFACTURER)
        pe.put("model", Build.MODEL)
        pe.put("version", Build.VERSION.RELEASE)
        pe.put("device", Build.DEVICE)
        pe.put("product", Build.PRODUCT)
        pe.put("hardware", Build.HARDWARE)
        pe.put("fingerprint", Build.FINGERPRINT)
        pe.put("display", Build.DISPLAY)
        pe.put("board", Build.BOARD)
        pe.put("bootloader", Build.BOOTLOADER)
        pe.put("id", Build.ID)
        pe.put("user", Build.USER)
        pe.put("host", Build.HOST)
        pe.put("sdk", Build.VERSION.SDK_INT)
        pe.put("isVPNConnected", isVPNConnected(context) ?: JSONObject.NULL)
        pe.put("networkInterfaces", getIFs())
        pe.put("androidId", getAndroidId(context))
//        pe.put("serial", Build.SERIAL)
//        pe.put("supportedAbis", Build.SUPPORTED_ABIS)
//        pe.put("supported32BitAbis", Build.SUPPORTED_32_BIT_ABIS)
//        pe.put("supported64BitAbis", Build.SUPPORTED_64_BIT_ABIS)
//        pe.put("supportedNotchSizes", Build.SUPPORTED_NOTCH_SIZES)
//        pe.put("location", getLocation())
        return pe
    }

    @SuppressLint("MissingPermission")
    fun isVPNConnected(context: Context): Boolean? {
        /**
         * Check if the app has the ACCESS_NETWORK_STATE permission.
         * Check if the VPN is connected if it already has permission.
         * Does not ask for any new permissions.
         */
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        } else {
            return null
        }
    }

    fun getIFs(): JSONObject {
        // Get the IP addresses for the active network interfaces
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        var netIFs = JSONObject()
        for (networkInterface in networkInterfaces) {
            if (networkInterface.isUp && !networkInterface.isLoopback && !networkInterface.isVirtual) {
                // skip loopback, virtual, and down interfaces
                var netIF = JSONObject()
                netIF.put("isUp", networkInterface.isUp)
                val inetAddresses = networkInterface.inetAddresses
                var netIP = JSONArray()
                for (inetAddress in inetAddresses) {
                    // skip loopback addresses
                    if (!(inetAddress.isLoopbackAddress || inetAddress.isLinkLocalAddress)) {
                        netIP.put(inetAddress.hostAddress)
                    }
                }
                if (netIP.length() > 0) {
                    netIF.put("hostAddresses", netIP)
                    netIFs.put(networkInterface.name, netIF)
                }
            }
        }
        return netIFs
    }

    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}