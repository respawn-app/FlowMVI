plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

kotlin {
    configureMultiplatform(this, wasmWasi = false)
}

android {
    namespace = "${Config.namespace}.debugger"
    configureAndroidLibrary(this)
}

dependencies {
    commonMainApi(projects.core)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.bundles.serialization)
    commonMainImplementation(libs.uuid)
}
