plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)
}
kotlin {
    jvm {
        compilations.all {
            compilerOptions.configure { jvmToolchain(21) }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.compose)
            implementation(projects.debugger.debuggerCommon)

            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.bundles.serialization)
            implementation(libs.bundles.ktor.server)
            implementation(applibs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.collections)
            implementation(applibs.apiresult)
            implementation(libs.uuid)
            implementation(applibs.bundles.koin)
            implementation(libs.kotlin.io)
            implementation(libs.kotlin.atomicfu)
        }
        jvmMain.dependencies {
            implementation(libs.kotlin.coroutines.swing)
            implementation(compose.desktop.common)
        }
    }
}
