@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    // id(libs.plugins.atomicfu.id)
    alias(libs.plugins.kotest)
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    configureMultiplatform(this)
}

android {
    namespace = Config.namespace
    configureAndroidLibrary(this)
}

publishAndroid()
publishMultiplatform()

dependencies {
    commonMainApi(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.kotlinx.atomicfu)
    "kotestTestImplementation"(libs.bundles.unittest)
}
