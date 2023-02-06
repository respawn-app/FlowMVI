plugins {
    id("pro.respawn.android-library")
}

publishAndroid()

android {
    namespace = "${Config.artifactId}.android.view"
}

dependencies {
    api(project(":android"))
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
