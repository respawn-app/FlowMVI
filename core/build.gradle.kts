@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("pro.respawn.shared-library")
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.maven.publish)

    // TODO: https://github.com/kotest/kotest/issues/3598
    // alias(libs.plugins.kotest)
    dokkaDocumentation
}

atomicfu {
    dependenciesVersion = libs.versions.kotlinx.atomicfu.get()
    transformJvm = true
    jvmVariant = "VH"
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
