import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm("desktop") {
        compilations.all {
            compilerOptions.configure { jvmToolchain(21) }
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.bundles.serialization)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(libs.apiresult)
            implementation(libs.uuid)
            implementation(libs.bundles.koin)
            implementation(libs.kotlin.io)
            implementation(projects.core)
            implementation(projects.savedstate)
            implementation(projects.compose)
        }
        desktopMain.apply {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "${Config.namespace}.debugger.app.MainKt"

        nativeDistributions {
            macOS {
                packageName = Config.namespace
                dockName = Config.debuggerPluginName
                setDockNameSameAsPackageName = false
            }
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = Config.namespace
            packageVersion = Config.versionName
        }
    }
}


dependencies {
    // debugImplementation(libs.compose.tooling)
}
