import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)

}
val props by localProperties

kotlin {
    jvm {
        compilations.all {
            compilerOptions.configure { jvmToolchain(21) }
        }
    }

    sourceSets {
        commonMain.dependencies {
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            implementation(libs.bundles.serialization)
            implementation(libs.bundles.ktor.server)
            implementation(libs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.collections)
            implementation(libs.apiresult)
            implementation(libs.uuid)
            implementation(libs.bundles.koin)
            implementation(libs.kotlin.io)
            implementation(libs.kotlin.atomicfu)

            implementation(projects.savedstate)
            implementation(projects.core)
            implementation(projects.compose)
            implementation(projects.debugger.common)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
    }
}
