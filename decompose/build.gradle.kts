plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
    id("maven-publish")
    signing
}

kotlin {
    configureMultiplatform(
        ext = this,
        tvOs = false,
        watchOs = false,
        linux = false,
        windows = false
    )
}

android {
    configureAndroidLibrary(this)
}

android {
    namespace = "${Config.namespace}.decompose"
}

dependencies {
    commonMainApi(projects.core)

    commonMainApi(libs.decompose)
    commonMainApi(libs.essenty.lifecycle)
    commonMainApi(libs.essenty.lifecycle.coroutines)
    commonMainApi(libs.essenty.instancekeeper)
}

publishMultiplatform()
