plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
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
        wasmWasi = false,
    )
}

android {
    namespace = "${Config.namespace}.debugger.plugin"
    configureAndroidLibrary(this)
}

dependencies {
    commonMainApi(projects.debugger.debuggerClient)

    commonMainImplementation(projects.debugger.debuggerCommon)
    commonMainImplementation(libs.bundles.ktor.client)
    commonMainImplementation(libs.ktor.client.engine)
    commonMainImplementation(libs.bundles.serialization)
}
