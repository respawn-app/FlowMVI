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
        wasmJs = false,
        windows = false,
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
    commonMainApi(projects.debugger.debuggerClient)

    commonMainImplementation(projects.debugger.debuggerCommon)
    commonMainImplementation(libs.bundles.ktor.client)
    commonMainImplementation(libs.ktor.client.engine)
    commonMainImplementation(libs.bundles.serialization)
    commonMainImplementation(libs.uuid)
}
