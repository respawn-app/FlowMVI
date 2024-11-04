plugins {
    id("pro.respawn.android-library")
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}

android {
    namespace = "${Config.artifactId}.android"
}

dependencies {
    api(projects.core)
    // api(libs.lifecycle.runtime)
    // api(libs.lifecycle.viewmodel)
    api(libs.kotlin.coroutines.android)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
