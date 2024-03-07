plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
    signing
}

android {
    configureAndroidLibrary(this)
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
        wasmJs = true
    )
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.compose.preview)
            implementation(libs.compose.lifecycle.viewmodel)
            implementation(libs.compose.lifecycle.runtime)
            api(projects.android)
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

android {
    namespace = "${Config.namespace}.compose"

    buildFeatures {
        compose = true
    }
}

dependencies {
    debugImplementation(libs.compose.tooling)
}

publishMultiplatform()
