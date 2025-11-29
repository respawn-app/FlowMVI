import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id(libs.plugins.kotlin.multiplatform.id)
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

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
        }
        jvmMain.dependencies {
            implementation(libs.compose.desktop)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
        }
    }
}
