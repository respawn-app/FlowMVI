plugins {
    id(libs.plugins.kotlin.multiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

android {
    namespace = "${Config.namespace}.essenty.compose"
    configureAndroidLibrary(this)

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
        wasmWasi = false,
    )
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui.tooling.preview)
        }
        commonMain.dependencies {
            api(projects.core)
            api(projects.compose)
            api(projects.essenty)

            api(libs.essenty.lifecycle)
            api(libs.essenty.lifecycle.coroutines)
            api(libs.essenty.instancekeeper)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
        }
        jvmMain.dependencies {
            implementation(libs.compose.desktop)
        }
    }
}
