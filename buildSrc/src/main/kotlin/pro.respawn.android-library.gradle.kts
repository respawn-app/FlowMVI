plugins {
    kotlin("android")
    id("com.android.library")
    id("maven-publish")
    signing
}

android {
    configureAndroidLibrary(this)
}

publishAndroid()
