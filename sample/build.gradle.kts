import Config.licenseFile
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)
}

private val pluginPrefix = "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination"

kotlin {
    applyDefaultHierarchyTemplate()

    // @OptIn(ExperimentalWasmDsl::class)
    // wasmJs {
    //     moduleName = "${Config.artifactId}.sample"
    //     nodejs()
    //     browser()
    //     binaries.library()
    // }
    // TODO: Enable wasm on kmputils and apiresult
    jvm("desktop")

    androidTarget()

    sequence {
        yield(iosX64())
        yield(iosArm64())
        yield(iosSimulatorArm64())
        yield(macosArm64())
        yield(macosX64())
    }.toList()

    sourceSets {
        val desktopMain by getting

        configurations.all {
            exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
        }
        all {
            languageSettings {
                progressiveMode = true
                languageVersion = Config.kotlinVersion.version
                Config.optIns.forEach { optIn(it) }
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            // @OptIn(ExperimentalComposeLibrary::class)
            // implementation(compose.desktop.components.splitPane)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation(libs.bundles.serialization)
            implementation(applibs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(applibs.apiresult)
            implementation(libs.uuid)
            implementation(applibs.bundles.koin)
            implementation(libs.kotlin.io)

            implementation(projects.core)
            implementation(projects.essenty.essentyCompose)
            implementation(projects.compose)
            implementation(projects.savedstate)
            implementation(projects.debugger.debuggerPlugin)
        }
        desktopMain.apply {
            dependencies {
                implementation(libs.kotlin.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }
    }

}
android {
    namespace = Config.artifactId
    configureAndroidLibrary(this)
}


compose.desktop {
    application {
        mainClass = "${Config.Sample.namespace}.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Exe)
            packageName = Config.namespace
            packageVersion = Config.majorVersionName
            description = Config.Sample.appDescription
            vendor = Config.vendorName
            licenseFile = rootProject.rootDir.resolve(Config.licenseFile)
            val iconDir = rootProject.rootDir.resolve("docs").resolve("images")
            macOS {
                packageName = Config.Sample.name
                dockName = Config.Sample.name
                setDockNameSameAsPackageName = false
                bundleID = Config.Sample.namespace
                appCategory = "public.app-category.developer-tools"
                iconFile = iconDir.resolve("icon_macos.icns")
            }
            windows {
                dirChooser = true
                menu = false
                shortcut = true
                perUserInstall = true
                upgradeUuid = Config.Sample.appId
                iconFile = iconDir.resolve("favicon.ico")
            }
            linux {
                debMaintainer = Config.supportEmail
                appCategory = "Development"
                iconFile = iconDir.resolve("icon_512.png")
            }
        }
    }
}
