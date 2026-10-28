import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "us.spur.monocle"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        val sdkVersion = providers.gradleProperty("monocleVersion").get()
        buildConfigField("String", "SDK_VERSION", "\"$sdkVersion\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.play.services.ads.identifier)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        // Emit Kotlin 2.1 metadata so apps on older Kotlin compilers (including the version
        // bundled with AGP 9) can compile against the SDK
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
    }
}
