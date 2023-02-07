plugins {
    id("pro.respawn.android-library")
}

android {
    namespace = "${Config.artifactId}.android.view"
}

dependencies {
    api(project(":core"))
    api(project(":android"))
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
