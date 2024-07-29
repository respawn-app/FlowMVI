@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("pro.respawn.shared-library")
    alias(libs.plugins.atomicfu)

    // TODO: https://github.com/kotest/kotest/issues/3598
    // alias(libs.plugins.kotest)
}

android {
    namespace = Config.namespace
}

dependencies {
    commonMainApi(libs.kotlin.coroutines.core)
    commonMainImplementation(libs.kotlin.atomicfu)

    // unfortunately kotest doesn't support all the targets that we support
    jvmTestImplementation(libs.bundles.unittest)
    jvmTestImplementation(projects.test)
}
