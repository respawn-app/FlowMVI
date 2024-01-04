plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    alias(libs.plugins.serialization)
}

private val PluginPrefix = "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination"

android {
    configureAndroid(this)
    namespace = "${Config.artifactId}.sample"
    compileSdk = Config.compileSdk

    defaultConfig {
        applicationId = Config.artifactId
        minSdk = Config.appMinSdk
        targetSdk = Config.targetSdk
        versionCode = 1
        versionName = Config.versionName
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
    kotlinOptions {
        freeCompilerArgs += buildList {
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                add("-P")
                add("$PluginPrefix=${layout.buildDirectory.get()}/compose_metrics")
                add("-P")
                add("$PluginPrefix=${layout.buildDirectory.get()}/compose_metrics")
            }
        }
        jvmTarget = Config.jvmTarget.target
        languageVersion = Config.kotlinVersion.version
    }
}

dependencies {
    implementation(projects.android)
    implementation(projects.compose)
    implementation(projects.androidView)
    implementation(projects.savedstate)

    implementation(libs.bundles.koin)
    implementation(libs.koin.android.compose)
    implementation(libs.kotlin.serialization.json)

    implementation(libs.androidx.core)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.activity)

    implementation(libs.compose.preview)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.material)

    debugImplementation(libs.compose.tooling)
}
