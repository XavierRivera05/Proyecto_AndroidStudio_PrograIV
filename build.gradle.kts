// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// build.gradle.kts (a nivel de proyecto)

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Añade esta línea con la versión más reciente del plugin
        classpath("com.google.gms:google-services:4.4.2") // Puedes usar la última versión disponible
    }
}
