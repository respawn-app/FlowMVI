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
            implementation(libs.kotlin.io)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlin.browser)
        }
        jvmTest.dependencies {
            implementation(libs.bundles.unittest)
            implementation(projects.test)
        }
    }
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.savedstate"
}
