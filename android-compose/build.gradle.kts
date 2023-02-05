plugins {
    id("pro.respawn.shared-library")
    id("pro.respawn.android-library")
}

kotlin {
    configureMultiplatform(
        this,
        android = true,
        ios = false,
        jvm = false,
    )
}

android {
    namespace = "${rootProject.group}.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
        useLiveLiterals = true
    }
}
//
// dependencies {
//     api(project(":android"))
//
//     implementation(libs.compose.ui)
//     implementation(libs.compose.foundation)
// }
