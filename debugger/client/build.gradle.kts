plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    alias(libs.plugins.serialization)
    signing
}
kotlin {
    configureMultiplatform(
        this,
        watchOs = false, // not supported by all needed ktor artifacts?
    )
}
android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.debugger.core"
}

dependencies {
    commonMainApi(projects.core)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.bundles.ktor)
    commonMainImplementation(libs.bundles.serialization)
    commonMainImplementation(libs.uuid)
}

publishMultiplatform()
