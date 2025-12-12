plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

kotlin {
    configureMultiplatform(this,)
}

android {
    namespace = "${Config.namespace}.debugger"
    configureAndroidLibrary(this)
}

dependencies {
    commonMainApi(projects.core)
    commonMainApi(projects.metrics)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.bundles.serialization)
}
