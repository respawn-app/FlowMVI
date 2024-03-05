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
            implementation(libs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(libs.apiresult)
            implementation(libs.uuid)
            implementation(libs.bundles.koin)
            implementation(libs.kotlin.io)
            implementation(projects.core)

            implementation(projects.debugger.server)
            implementation(projects.debugger.debuggerCommon)
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
        buildTypes.release.proguard {
            obfuscate = false
            optimize = false // TODO: Solve the issues with ktor and compose-desktop...
            configurationFiles.from(projectDir.resolve("desktop-rules.pro"))
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Exe)
            packageName = Config.namespace
            packageVersion = Config.majorVersionName
            description = Config.debuggerPluginDescription
            vendor = Config.vendorName
            licenseFile = rootProject.rootDir.resolve(Config.licenseFile)
            val iconDir = rootProject.rootDir.resolve("docs").resolve("images")
            macOS {
                packageName = Config.debuggerName
                dockName = Config.debuggerName
                setDockNameSameAsPackageName = false
                bundleID = "${Config.namespace}.debugger"
                appCategory = "public.app-category.developer-tools"
                iconFile = iconDir.resolve("icon_macos.icns")
            }
            windows {
                dirChooser = true
                menu = false
                perUserInstall = true
                upgradeUuid = Config.debuggerAppId
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

dependencies {
    // debugImplementation(libs.compose.tooling)
}
