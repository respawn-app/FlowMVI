plugins {
    id("com.nek12.android-library")
}

android {
    namespace = "${rootProject.group}.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
        useLiveLiterals = true
    }
}


dependencies {
    api(project(":android"))

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
}
