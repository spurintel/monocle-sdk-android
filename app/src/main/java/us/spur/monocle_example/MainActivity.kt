package us.spur.monocle_example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import us.spur.monocle_example.ui.theme.DemoAppTheme
import us.spur.monocle.BundleResponse
import us.spur.monocle.MonocleClient
import us.spur.monocle.MonoclePlugins
import us.spur.monocle.MonocleService
import us.spur.monocle.PlatformEval

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

        val monoclePlugins = MonoclePlugins("somescriptid")
        val monocleService: MonocleService = MonocleClient.apiService

        monocleService.getBundle(
            "CHANGEME",
            "0.0.20",
            "983e37d2-ff2f-4e6e-9fe4-e195f76f97cc",
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
        val eval = PlatformEval().getPlatformEval(context = this).toString(2)
        addLogEntry("Platform evaluation:\n" + eval)
    }
}

private val logEntries = mutableStateListOf<String>()

fun addLogEntry(entry: String) {
    logEntries.add(entry)
}
@Composable
fun ScrollingLog(logEntries: List<String>) {
    LazyColumn(
        reverseLayout = false
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