plugins {
    id("pro.respawn.android-library")
}

android {
    namespace = "${Config.namespace}.android.compose"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.core)
    api(projects.android)
    implementation(libs.compose.foundation)
    implementation(libs.compose.preview)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.lifecycle.runtime)
}
