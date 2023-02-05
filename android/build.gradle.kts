plugins {
    id("pro.respawn.android-library")
}

android {
    namespace = "${rootProject.group}.android"
}

dependencies {
    api(project(":core"))
    api(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.android)
}
