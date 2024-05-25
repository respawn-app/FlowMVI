import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm("desktop") {
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
            implementation(compose.components.resources)

            implementation(applibs.apiresult)
            implementation(applibs.bundles.kmputils)
            implementation(applibs.bundles.koin)

            implementation(libs.bundles.serialization)
            implementation(libs.kotlin.datetime)
            implementation(libs.uuid)
            implementation(libs.kotlin.io)

            implementation(projects.core)
            implementation(projects.debugger.server)
            implementation(projects.debugger.debuggerCommon)
            implementation(projects.compose)
        }
        desktopMain.apply {
            dependencies {
                implementation(libs.kotlin.coroutines.swing)
                implementation(compose.desktop.currentOs)
                implementation(compose.preview)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "${Config.Debugger.namespace}.app.MainKt"

        buildTypes.release.proguard {
            obfuscate = false
            optimize = false // TODO: Solve the issues with ktor and compose-desktop...
            configurationFiles.from(projectDir.resolve("desktop-rules.pro"))
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Exe)
            packageName = Config.Debugger.namespace
            packageVersion = Config.majorVersionName
            description = Config.Debugger.appDescription
            vendor = Config.vendorName
            licenseFile = rootProject.rootDir.resolve(Config.licenseFile)
            val iconDir = rootProject.rootDir.resolve("docs").resolve("images")
            macOS {
                packageName = Config.Debugger.name
                dockName = Config.Debugger.name
                setDockNameSameAsPackageName = false
                bundleID = Config.Debugger.namespace
                appCategory = "public.app-category.developer-tools"
                iconFile = iconDir.resolve("icon_macos.icns")
                minimumSystemVersion = "12.0"
            }
            windows {
                dirChooser = true
                menu = false
                shortcut = true
                perUserInstall = true
                upgradeUuid = Config.Debugger.appId
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
