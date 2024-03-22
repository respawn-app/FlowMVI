plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
    signing
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.essenty.compose"

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
            api(projects.android)

            implementation(libs.compose.foundation)
            implementation(libs.compose.preview)
            implementation(libs.compose.lifecycle.runtime)
        }
        commonMain.dependencies {
            api(projects.core)
            api(projects.compose)

            api(libs.essenty.lifecycle)
            api(libs.essenty.lifecycle.coroutines)
            api(libs.essenty.instancekeeper)

            implementation(compose.runtime)
            implementation(compose.foundation)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
    }
}

publishMultiplatform()

dependencies {
}
