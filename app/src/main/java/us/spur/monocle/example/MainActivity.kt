package us.spur.monocle.example

import android.os.Bundle
import android.util.Log
import android.content.Context
import android.provider.Settings
import java.util.UUID
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import us.spur.monocle.example.ui.theme.DemoAppTheme
import us.spur.monocle.sdk.BundleResponse
import us.spur.monocle.sdk.MonocleClient
import us.spur.monocle.sdk.MonoclePlugins
import us.spur.monocle.sdk.MonocleService
import us.spur.monocle.sdk.PlatformEval

/**
 * Example app utilizing the Spur Monocle SDK.
 *
 * This is an example app that demonstrates how to use the
 * Monocle SDK to load Monocle, perform analysis on whether the device
 * is using a VPN/Proxy and and fetch the results in an encrypted bundle.
 *
 * @author      spur.us
 * @see         https://github.com/spur/monocle-sdk-android
 * @see         https://docs.spur.us/#/monocle?id=monocle
 * @version     %I%, %G%
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DemoAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScrollingLog(logEntries = logEntries)
                    Log.d("API_CALL", "HERE")
                }
            }
        }

        val scriptId = getUUID(this)
        val monoclePlugins = MonoclePlugins("monocle-plugin-uuid")
        val monocleService: MonocleService = MonocleClient.apiService

        monocleService.getBundle(
            "CHANGEME",
            "0.0.20",
            scriptId,
            "android",
            monoclePlugins)!!.enqueue(object : Callback<BundleResponse?> {
            override fun onResponse(call: Call<BundleResponse?>, response: Response<BundleResponse?>) {
                if (response.isSuccessful()) {
                    Log.d("MonocleBundle", "is successful")
                    val bundleResponse: BundleResponse? = response.body()
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val jsonString = gson.toJson(bundleResponse)
                    addLogEntry("Monocle bundle received:\n" + jsonString)
                    Log.d("MonocleBundle", jsonString)
                } else {
                    addLogEntry("Monocle bundle not received")
                    Log.d("MonocleBundle", "not successful")
                }
            }
            
            override fun onFailure(call: Call<BundleResponse?>, t: Throwable) {
                Log.e("MainActivity", "Error fetching user data", t)
            }
        })
        val eval = PlatformEval().getPlatformEval(this).toString(2)
        addLogEntry("Platform evaluation:\n" + eval)
    }
}

private val logEntries = mutableStateListOf<String>()

fun addLogEntry(entry: String) {
    logEntries.add(0, entry)
}

/**
 * Returns a UUID based on the Android ID.
 *
 * The Android ID is no longer device specific, but device and app specific for privacy reasons.
 * If the Android ID is not available, a random UUID is generated.
 * @param context The application context.
 */
fun getUUID(context: Context): String {
    return try {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val uuid3 = UUID.nameUUIDFromBytes(androidId.toByteArray())
        uuid3.toString()
    } catch (e: Exception) {
        UUID.randomUUID().toString()  // UUIDv4
    }
}

@Composable
fun ScrollingLog(logEntries: List<String>) {
    val listState = rememberLazyListState()

    LazyColumn(
        reverseLayout = false,
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        // Add each log entry as a list item
        items(logEntries) { entry ->
            Text(text = entry + "\n")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScrollingLogPreview() {
    DemoAppTheme {
        ScrollingLog(logEntries = logEntries)
    }
}