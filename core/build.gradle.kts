@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("pro.respawn.shared-library")
    alias(libs.plugins.kotest)
}

android {
    namespace = Config.namespace
}

dependencies {
    commonMainCompileOnly(project(":annotations"))
    commonMainApi(libs.kotlin.coroutines.core)
    commonMainImplementation(libs.kotlin.atomicfu)
    // unfortunately kotest doesn't support all the targets that we support
    jvmTestImplementation(libs.bundles.unittest)
    jvmTestImplementation(project(":test"))
}
