package us.spur.monoclesdk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import us.spur.monocle.Monocle
import us.spur.monocle.MonocleConfig

class MainActivity : AppCompatActivity() {

    // Location is optional: run the assessment whether or not the user grants it
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        runMonocleAssessment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupMonocle()

        if (hasLocationPermission()) {
            runMonocleAssessment()
        } else {
            requestLocationPermissions()
        }
    }

    // This method sets up us.spur.monocle.Monocle. In a real app, call this once from
    // your Application class.
    private fun setupMonocle() {
        val publishableKey = getString(R.string.publishable_key)
        // If you do not want all the plugins to run, you can specify the ones you want
//        val config = MonocleConfig(
//            token = publishableKey,
//            enabledPlugins = MonoclePluginOptions.DNS or MonoclePluginOptions.LOCATION,
//        )
        val config = MonocleConfig(token = publishableKey)
        Monocle.setup(config, applicationContext)
    }

    private fun runMonocleAssessment() {
        lifecycleScope.launch {
            val assessmentResult = Monocle.getInstance().assess()
            findViewById<TextView>(R.id.assessmentResultTextView).text = assessmentResult.fold(
                onSuccess = { response -> response.data },
                onFailure = { error -> "Assessment failed: ${error.message}" }
            )
        }
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
}
