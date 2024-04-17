import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.serialization)
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    configureMultiplatform(this) {
        common {
            group("nonBrowser") {
                withJvm()
                withNative()
                withAndroidTarget()
            }
            group("browser") {
                withWasm()
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
    }
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.savedstate"
}

publishMultiplatform()
