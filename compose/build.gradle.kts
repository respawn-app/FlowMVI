import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.compose"

    buildFeatures {
        compose = true
    }
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
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
        wasmWasi = false,
        windows = false,
    )
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(libs.lifecycle.runtime)
            api(libs.lifecycle.compose)

            implementation(compose.runtime)
            implementation(compose.foundation)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(compose.uiTooling)
        }
    }
}
