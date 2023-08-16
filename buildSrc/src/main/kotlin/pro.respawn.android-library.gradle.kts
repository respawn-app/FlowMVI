plugins {
    kotlin("android")
    id("com.android.library")
    id("maven-publish")
    signing
}

android {
    configureAndroidLibrary(this)
    publishAndroid(this)

    kotlinOptions {
        freeCompilerArgs += Config.jvmCompilerArgs
        jvmTarget = Config.jvmTarget.target
        languageVersion = Config.kotlinVersion.version
    }
}
