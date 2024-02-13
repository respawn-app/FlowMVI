plugins {
    id("pro.respawn.shared-library")
    alias(libs.plugins.serialization)
}

android {
    namespace = "${Config.namespace}.savedstate"
}

dependencies {
    commonMainApi(projects.core)
    commonMainApi(libs.kotlin.serialization.json)
    commonMainImplementation(libs.kotlin.atomicfu)
    commonMainImplementation(libs.kotlin.io)

    androidMainApi(libs.lifecycle.savedstate)
}
