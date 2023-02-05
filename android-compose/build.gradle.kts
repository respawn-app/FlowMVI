plugins {
    kotlin("android")
    id("pro.respawn.android-library")
}

configurePublication()

android {
    namespace = "${Config.artifactId}.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
        useLiveLiterals = true
    }
}

dependencies {
    api(project(":android"))

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
}
