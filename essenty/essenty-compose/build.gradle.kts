plugins {
    id(libs.plugins.kotlinMultiplatform.id)
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
            implementation(compose.foundation)
            implementation(compose.preview)
        }
        commonMain.dependencies {
            api(projects.core)
            api(projects.compose)
            api(projects.essenty)

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
