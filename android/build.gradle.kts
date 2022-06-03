plugins {
    id("com.nek12.android-library")
}

android {
    namespace = "${rootProject.group}.android"
}

dependencies {
    api(project(":core"))
    api("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
}
