import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.id)
}

kotlin {
    configureMultiplatform(
        ext = this,
        wasmWasi = false,
        android = false,
    )
}

dependencies {
    commonMainImplementation(projects.core)
}
