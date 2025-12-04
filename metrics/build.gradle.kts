import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id(libs.plugins.kotlin.multiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.serialization)
    dokkaDocumentation
}

atomicfu {
    transformJvm = true
    jvmVariant = "VH"
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    configureMultiplatform(
        ext = this,
        wasmWasi = true, // datetime does not support wasmWasi
    )
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.metrics"
}

dependencies {
    commonMainApi(projects.core)
    commonMainImplementation(libs.kotlin.datetime)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.kotlin.serialization)
    commonMainImplementation(libs.kotlin.serialization.json)
}
