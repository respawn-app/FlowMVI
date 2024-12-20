import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.serialization)
    id(libs.plugins.kotlin.multiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    configureMultiplatform(this, wasmWasi = false) {
        common {
            group("nonBrowser") {
                withJvm()
                withNative()
                withAndroidTarget()
            }
            group("browser") {
                withWasmJs()
                withJs()
            }
        }
    }

    sourceSets {
        val nonBrowserMain by getting
        nativeMain.dependencies {
            implementation(libs.kotlin.io)
        }
        androidMain.dependencies {
            api(libs.lifecycle.savedstate)
        }
        commonMain.dependencies {
            api(projects.core)
            api(libs.kotlin.serialization.json)
            implementation(libs.kotlin.atomicfu)
        }
        nonBrowserMain.dependencies {
            implementation(libs.kotlin.io)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlin.browser)
        }
    }
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.savedstate"
}
