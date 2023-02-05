plugins {
    kotlin("android")
    id("pro.respawn.android-library")
}

android {
    namespace = "${Config.artifactId}.android"
}

dependencies {
    api(project(":core"))
    api(libs.lifecycle.runtime)
    api(libs.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.android)
}
