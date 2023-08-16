@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    // id(libs.plugins.atomicfu.id)
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    configureMultiplatform(this)
}

android {
    namespace = "${Config.namespace}.test"
    configureAndroidLibrary(this)
    publishAndroid(this)
}

publishMultiplatform()

dependencies {
    commonMainApi(project(":core"))
    commonMainApi(libs.kotlin.coroutines.core)
    commonMainApi(libs.kotlin.test)
    commonMainApi(libs.kotlin.coroutines.test)
}
