plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
    signing
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.decompose.compose"

    buildFeatures {
        compose = true
    }
}

kotlin {
    configureMultiplatform(
        ext = this,
        tvOs = false,
        watchOs = false,
        linux = false,
        windows = false,
    )
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.compose.preview)
            implementation(libs.compose.lifecycle.runtime)
            api(projects.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            api(projects.core)
            api(projects.compose)

            api(libs.essenty.lifecycle)
            api(libs.essenty.lifecycle.coroutines)
            api(libs.essenty.instancekeeper)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
            implementation(libs.compose.lifecycle.runtime)
        }
    }
}

publishMultiplatform()

dependencies {
}
