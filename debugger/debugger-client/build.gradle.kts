plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}
kotlin {
    configureMultiplatform(
        this,
        // not supported by all needed ktor artifacts?
        watchOs = false,
        wasmJs = false,
    )
}
android {
    namespace = "${Config.namespace}.debugger.client"
    configureAndroidLibrary(this)
}

dependencies {
    commonMainApi(projects.core)

    commonMainImplementation(projects.debugger.debuggerCommon)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.bundles.ktor.client)
    commonMainImplementation(libs.bundles.serialization)
    commonMainImplementation(libs.uuid)
}
