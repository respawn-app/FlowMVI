plugins {
    kotlin("multiplatform")
    id("com.android.library")
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
    namespace = "${Config.namespace}.decompose"
}

dependencies {
    commonMainApi(projects.core)

    commonMainApi(libs.essenty.lifecycle)
    commonMainApi(libs.essenty.instancekeeper)
}

publishMultiplatform()
