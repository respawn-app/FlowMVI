import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id(libs.plugins.kotlin.multiplatform.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop") {
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.animation)
            implementation(libs.compose.animation.graphics)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(applibs.decompose)
            implementation(applibs.decompose.compose)
            implementation(applibs.bundles.koin)

            implementation(projects.debugger.server)
        }
        desktopMain.apply {
            dependencies {
                implementation(libs.kotlin.coroutines.swing)
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.ui.tooling.preview)
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
            val iconDir = rootProject.rootDir.resolve("docs").resolve("static")
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
                iconFile = iconDir.resolve("icon-512.png")
            }
        }
    }
}
