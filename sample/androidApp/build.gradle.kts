plugins {
    kotlin("android")
    id(applibs.plugins.android.application.id)
    id("kotlin-parcelize")
}

android {
    configureAndroid(this)
    namespace = "${Config.Sample.namespace}.app"
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
    implementation(projects.sample)

    implementation(libs.kotlin.serialization.json)
    implementation(applibs.bundles.kmputils)
    implementation(applibs.androidx.splashscreen)

    implementation(applibs.compose.activity)
    implementation(applibs.decompose.compose)
    implementation(applibs.decompose)
    implementation(applibs.koin.android)
    implementation(applibs.koin.android.compose)
    implementation(applibs.view.material)

    debugImplementation(libs.compose.tooling)
}
