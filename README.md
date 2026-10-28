# monocle-sdk-android

## Prerequisites
* Android Studio, or another IDE with Gradle and Kotlin support
* Android 7.0 (API level 24) or later
* Spur account - https://app.spur.us/start/create-account
* Your Monocle app's **Publishable key**, from the app's **Settings** page under **API Keys** in the [Monocle Dashboard](https://app.spur.us/monocle)

## Installation

The Monocle SDK isn't on Maven Central yet. Add it to your project as an Android Archive (`.aar`) file:

1. Download `monocle.aar` from the latest release on the [Releases page](https://github.com/spurintel/monocle-sdk-android/releases).
2. Create a `libs` folder in your app module (for example `app/libs`), if it doesn't have one, and copy `monocle.aar` into it.
3. In the module's `build.gradle.kts`, add the archive and the libraries the SDK uses. An `.aar` added this way doesn't bring its dependencies with it, so all of these are needed:

```kotlin
dependencies {
    implementation(files("libs/monocle.aar"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("androidx.core:core-ktx:1.18.0")
}
```

4. Sync the project with Gradle.

## Permissions

The SDK doesn't add any permissions to your app. Declare the ones you need in your app's `AndroidManifest.xml`:

```xml
<!-- Required -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Optional: lets the assessment report the network type -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Optional: includes device location in the assessment -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

The SDK never asks the user for location permission. If your app has been granted it, the assessment includes the device's location; otherwise the assessment runs without it.

## Usage

1. Set up Monocle once, in your `Application` class, with your Publishable key:

```kotlin
import us.spur.monocle.Monocle
import us.spur.monocle.MonocleConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Monocle.setup(MonocleConfig(token = "<MONOCLE PUBLISHABLE KEY>"), this)
    }
}
```

2. Run an assessment from a coroutine when you need one, such as before sign-in or checkout:

```kotlin
lifecycleScope.launch {
    Monocle.getInstance().assess()
        .onSuccess { response ->
            val encryptedAssessment = response.data
            // Send encryptedAssessment to your backend
        }
        .onFailure { error ->
            // Handle the error
        }
}
```

3. Send the encrypted assessment to your backend and decrypt it there. See the [Monocle documentation](https://docs.spur.us/monocle).

### Configuration options

| Option | Type | Description |
| - | - | - |
| `token` | `String` | Required. Your app's Publishable key. |
| `cpd` | `String?` | Optional customer-provided data, returned in the assessment. |
| `enabledPlugins` | `Int` | Which device signals to collect: `MonoclePluginOptions.DNS`, `DEVICE_INFO` and `LOCATION`, combined with `or`. Defaults to `MonoclePluginOptions.ALL`. |

## Example app

* [Monocle Android Example App](https://github.com/spurintel/monocle-example-android)

## FAQ

### Can't I just use the native network state APIs to determine if the device is on a VPN?

You can and possibly should use native APIs as well, but there are several situations where this information is inaccurate.  These APIs also generally require additional system permissions the user has to approve, and are increasingly restrictive in recent Android versions.  The native APIs also will not tell you which proxy/VPN service is in use or provide additional enrichment to make more subtle access decisions.

### Does Monocle support iOS?
Yes. See [Monocle SDK for iOS](https://github.com/spurintel/monocle-sdk-ios)

### What about Flutter or ReactNative or other frameworks?
Monocle is lightweight and should work on any platform that can execute Javascript in the client and make standard HTTPS GETs/POSTs, but it is untested at this time.  Please let us know if you try it.