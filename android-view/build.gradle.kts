plugins {
    kotlin("android")
    id("pro.respawn.android-library")
}

configurePublication()

android {
    namespace = "${Config.artifactId}.android.view"
}

dependencies {
    api(project(":android"))
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
