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
    publishAndroid(this)
}

publishMultiplatform()

dependencies {
    commonMainApi(libs.kotlin.coroutines.core)
    commonMainImplementation(libs.kotlin.atomicfu)
    "kotestTestImplementation"(libs.bundles.unittest)
    "kotestTestImplementation"(project(":test"))
}
