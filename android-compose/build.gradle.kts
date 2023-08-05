plugins {
    id("pro.respawn.android-library")
}

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
    api(project(":core"))
    api(project(":android"))

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.preview)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.lifecycle.runtime)
}
