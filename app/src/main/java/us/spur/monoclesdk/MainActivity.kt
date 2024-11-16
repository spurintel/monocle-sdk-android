package us.spur.monoclesdk

import us.spur.monocle.MonocleConfig
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import us.spur.monocle.AssessmentResult
import us.spur.monocle.Monocle

class MainActivity : AppCompatActivity() {


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            setupMonocle()
            CoroutineScope(Dispatchers.Main).launch {
                runMonocleAssessment()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (hasLocationPermission()) {
            setupMonocle()
            CoroutineScope(Dispatchers.Main).launch {
                runMonocleAssessment()
            }
        } else {
            // Request the location permissions
            requestLocationPermissions()
        }
    }

    // This method sets up us.spur.monocle.Monocle
    private fun setupMonocle() {
        val siteToken = getString(R.string.site_token)
        val config = MonocleConfig(token = siteToken)
        Monocle.setup(config, this, R::class.java)
    }

    private suspend fun runMonocleAssessment() {
        val monocle = Monocle.getInstance()
        val assessmentResult = monocle.assess()
        assessmentResult.fold(
            onSuccess = { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        findViewById<TextView>(R.id.assessmentResultTextView).text = response.data
                    }
                }
            },
            onFailure = { error ->
                runOnUiThread {
                    // Assuming you have a TextView with the id `assessmentResultTextView`
                    findViewById<TextView>(R.id.assessmentResultTextView).text = "Assessment failed: ${error.message}"
                }
            }
        )
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun formatAssessmentResult(result: AssessmentResult): String {
        return StringBuilder().apply {
            append("VPN: ${result.vpn}\n")
            append("Proxied: ${result.proxied}\n")
            append("Anonymous: ${result.anon}\n")
            append("RDP: ${result.rdp}\n")
            append("DCH: ${result.dch}\n")
            append("Country Code: ${result.cc}\n")
            append("IP Address: ${result.ip}\n")
            result.ipv6?.let { append("IPv6 Address: $it\n") }
            append("Timestamp: ${result.ts}\n")
            append("Complete: ${result.complete}\n")
            append("ID: ${result.id}\n")
            append("Session ID: ${result.sid}\n")
        }.toString()
    }
}