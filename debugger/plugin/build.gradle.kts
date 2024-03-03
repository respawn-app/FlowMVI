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
        watchOs = false,
        tvOs = false,
        linux = false,
        js = false,
    )
}

android {
    configureAndroidLibrary(this)
}

publishMultiplatform()

android {
    namespace = "${Config.namespace}.debugger.plugin"
}

dependencies {
    commonMainApi(projects.debugger.client)
    commonMainApi(projects.debugger.common)
    commonMainImplementation(libs.bundles.ktor.client)
    commonMainImplementation(libs.ktor.client.engine)
    commonMainImplementation(libs.bundles.serialization)
    commonMainImplementation(libs.uuid)
}
