plugins {
    id("com.nek12.android-library")
}

android {
    namespace = "${rootProject.group}.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
        useLiveLiterals = true
    }
}


dependencies {
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation:${Versions.compose}")
    api(project(":android"))
}
