plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
}

kotlin {
    configureMultiplatform(this, explicitApi = false)
}

android {
    configureAndroidLibrary(this)
}

android {
    namespace = "${Config.namespace}.debugger"
}

dependencies {
    commonMainApi(projects.core)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.bundles.serialization)
    commonMainImplementation(libs.uuid)
}
