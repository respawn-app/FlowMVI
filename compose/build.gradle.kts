plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
    signing
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.compose"

    buildFeatures {
        compose = true
    }
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
        linux = false,
        js = true,
        wasmJs = true,
        windows = false,
    )
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.compose.preview)
            implementation(projects.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            api(projects.core)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
    }
}

dependencies {
    debugImplementation(libs.compose.tooling)
}

publishMultiplatform()
