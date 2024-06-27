# monocle-sdk-android
Monocle SDK for Android




## Manual dependency inclusion

To include the Monocle .aar file as a dependency in your Android project, you can follow these steps:
1. Create a libs Directory:
If you don't have one already, create a directory named libs at the root level of your project.
2. Place the .aar File:
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

### Explanation:
The repositories block with flatDir tells Gradle to look for dependencies in the libs directory.
The implementation line in the dependencies block includes the .aar file as a dependency, making its classes and resources available to your module.

### Important Notes:
Replace monocle.aar with the actual name of your .aar file.
If you encounter any issues, double-check that the .aar file is correctly placed in the libs directory and that the file path in the implementation line is accurate.
If the Monocle library has any transitive dependencies (dependencies of its own), you might need to include those as well in your build.gradle file.
By following these steps, you should be able to successfully include the Monocle .aar file as a dependency in your Android project and utilize its functionality