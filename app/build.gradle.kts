plugins {
    id("com.android.application")
    kotlin("android")
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
    kotlinOptions {
        freeCompilerArgs += Config.jvmCompilerArgs
        jvmTarget = Config.jvmTarget.target
        languageVersion = Config.kotlinVersion.version
    }
}

dependencies {
    implementation(project(":android"))
    implementation(project(":android-compose"))
    implementation(project(":android-view"))

    implementation(libs.bundles.koin)
    implementation(libs.koin.compose)

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
