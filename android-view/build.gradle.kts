plugins {
    id("pro.respawn.android-library")
}

android {
    namespace = "${Config.artifactId}.android.view"
}

dependencies {
    api(projects.core)
    api(projects.android)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
