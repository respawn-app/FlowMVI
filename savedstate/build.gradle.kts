plugins {
    id("pro.respawn.shared-library")
    alias(libs.plugins.serialization)
}

android {
    namespace = "${Config.namespace}.savedstate"
}

dependencies {
    commonMainApi(projects.core)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.kotlin.io)
    commonMainImplementation(libs.kotlin.serialization.json)
    androidMainImplementation(libs.lifecycle.savedstate)
}
