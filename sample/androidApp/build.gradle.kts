plugins {
    kotlin("android")
    id(applibs.plugins.android.application.id)
    id("kotlin-parcelize")
}

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
}

dependencies {
    implementation(projects.android)
    implementation(projects.compose)
    implementation(projects.savedstate)
    implementation(projects.debugger.debuggerPlugin)

    implementation(applibs.bundles.koin)
    implementation(applibs.koin.android.compose)
    implementation(libs.kotlin.serialization.json)

    implementation(libs.androidx.core)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(applibs.compose.activity)

    implementation(libs.compose.preview)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(applibs.view.material)
    implementation(libs.compose.lifecycle.viewmodel)

    debugImplementation(libs.compose.tooling)
}
