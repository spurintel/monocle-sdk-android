# monocle-sdk-android

## Prerequisites
* AndroidStudio or other with Gradle and Kotlin support - Tested up to Android Studio Koala | 2024.1.1
* Android - Emulated in Android Studio or physical device tested up to Android 15 beta.
* Spur account - https://app.spur.us/start/create-account
* Monocle Site Token - https://app.spur.us/monocle

## Implementation
1. In the target Android project, add a package dependency for Monocle.  See dependency resolution options below.
2. import the `Monocle` package in a Kotlin source file
```kotlin
import us.spur.monocle.Monocle
import us.spur.monocle.MonocleConfig
```
3. Get a Monocle **site-token** from the [Monocle management interface](https://app.spur.us/monocle)
4. Enter your site token as the `token` parameter
5. Instantiate Monocle 
```kotlin
val config = MonocleConfig(token = siteToken)
Monocle.setup(config, this)
```
6. Call `assess` to run the assessment.
```kotlin
val monocle = Monocle.getInstance()
val assessmentResult = monocle.assess()
```
7. Pass the resulting assessment to your [backend integration](https://docs.spur.us/monocle?id=backend-integration).

### Automatic dependency resolution

The Monocle SDK is not hosted in Maven Central yet, but that is the intent and priority.

At this time you would need to copy the entire Monocle source module, or check out this repository and build it to generate the appropriate Monocle Android Archive (.aar) file for use in a separate project.  See [Monocle Android Example App](https://github.com/spurintel/monocle-example-android) for a complete example using a monocle.aar file.

### Manual dependency inclusion

#### Getting the AAR from GitHub

To get the Monocle .aar file from GitHub, follow these steps:
1. Go to the [Releases page](https://github.com/spurintel/monocle-sdk-android/releases) of the repository.
2. Download the latest `monocle.aar` file from the assets section of the latest release.

#### Including the AAR in Your Project
To include the Monocle .aar file as a dependency in your Android project, you can follow these steps:
1. Create a libs Directory:
   If you don't have one already, create a directory named libs at the root level of your project.
2. Place the .aar file:
   Copy the Monocle .aar file into the libs directory you just created.
3. Modify build.gradle(.kts) (Module Level):
   Open the build.gradle(.kts) file of the module where you want to use the Monocle library.
   Add the following lines within the android block:
```gradle
android {
    // ... other configurations

    repositories {
        flatDir {
            dirs("libs")
            // dirs 'libs' // if not .kts
        }
    }
}
```
Add the dependency for the .aar file in the dependencies block:
```gradle
dependencies {
    // ... other dependencies

    implementation(files("libs/monocle.aar")) // Replace 'monocle.aar' with the actual file name
}
```
4. Sync Project:
   After making these changes, click "Sync Now" in the bar that appears at the top of Android Studio to synchronize your project with the updated Gradle files.

#### Explanation:
The repositories block with flatDir tells Gradle to look for dependencies in the libs directory.
The implementation line in the dependencies block includes the .aar file as a dependency, making its classes and resources available to your module.

#### Important Notes:
Replace monocle.aar with the actual name of your .aar file.
If you encounter any issues, double-check that the .aar file is correctly placed in the libs directory and that the file path in the implementation line is accurate.
If the Monocle library has any transitive dependencies (dependencies of its own), you might need to include those as well in your build.gradle file.
By following these steps, you should be able to successfully include the Monocle .aar file as a dependency in your Android project and utilize its functionality

## Example app

* [Monocle Android Example App](https://github.com/spurintel/monocle-example-android)

## FAQ

### Can't I just use the native network state APIs to determine if the device is on a VPN?

You can and possibly should use native APIs as well, but there are several situations where this information is inaccurate.  These APIs also generally require additional system permissions the user has to approve, and are increasingly restrictive in recent Android versions.  The native APIs also will not tell you which proxy/VPN service is in use or provide additional enrichment to make more subtle access decisions.

### Does Monocle support iOS?
Yes. See [Monocle SDK for iOS](https://github.com/spurintel/monocle-sdk-ios)

### What about Flutter or ReactNative or other frameworks?
Monocle is lightweight and should work on any platform that can execute Javascript in the client and make standard HTTPS GETs/POSTs, but it is untested at this time.  Please let us know if you try it.