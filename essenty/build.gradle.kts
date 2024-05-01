import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
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
    namespace = "${Config.namespace}.essenty"
}

dependencies {
    commonMainApi(projects.core)
    commonMainApi(projects.savedstate)

    commonMainApi(libs.essenty.lifecycle)
    commonMainApi(libs.essenty.instancekeeper)
    commonMainApi(libs.essenty.statekeeper)
    commonMainApi(libs.essenty.lifecycle.coroutines)
}

publishMultiplatform()
