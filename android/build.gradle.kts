plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

kotlin {
    configureMultiplatform(
        ext = this,
        jvm = true,
        android = true,
        iOs = true,
        macOs = true,
        watchOs = false,
        tvOs = false,
        windows = false,
        linux = true,
        js = true,
        wasmJs = true,
        wasmWasi = false,
    )

    sourceSets.androidMain.dependencies {
        api(libs.kotlin.coroutines.android)
        api(libs.androidx.fragment)
        api(libs.androidx.activity)
    }
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.android"
}

dependencies {
    commonMainApi(projects.core)
    commonMainApi(libs.lifecycle.runtime)
    commonMainApi(libs.lifecycle.viewmodel)
}
