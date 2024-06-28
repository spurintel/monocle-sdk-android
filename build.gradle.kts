// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    id("org.jetbrains.dokka") version "1.9.20"
}
dependencies {
    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:1.9.20")
}

tasks.dokkaHtml.configure {
    outputDirectory.set(file("../docs"))
}