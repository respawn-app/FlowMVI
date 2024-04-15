import org.intellij.lang.annotations.Language
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)
}

@Language("Kotlin")
// language=kotlin
val BuildConfig = """
    package ${Config.namespace}

    internal object BuildFlags {
        const val VersionName = "${Config.versionName}"
        const val SupportEmail = "${Config.supportEmail}"
        const val ProjectDescription = "${Config.Sample.appDescription}"
    }
""".trimIndent()

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
        // yield(macosArm64())
        // yield(macosX64())
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
            implementation(compose.materialIconsExtended)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation(libs.bundles.serialization)
            implementation(libs.kotlin.datetime)
            implementation(libs.uuid)
            implementation(libs.kotlin.io)

            implementation(applibs.bundles.kmputils)
            implementation(applibs.bundles.koin)
            implementation(applibs.apiresult)
            implementation(applibs.decompose.compose)
            implementation(applibs.compose.codehighlighting)
            implementation(applibs.decompose)

            implementation(projects.core)
            implementation(projects.essenty.essentyCompose)
            implementation(projects.compose)
            implementation(projects.savedstate)
        }
        nativeMain.dependencies {
            implementation(projects.debugger.debuggerPlugin)
        }
        desktopMain.dependencies {
            implementation(projects.debugger.debuggerPlugin)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.desktop.components.splitPane)
            implementation(libs.kotlin.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }
        androidMain.dependencies {
            implementation(applibs.koin.android)
        }
    } // sets
}
android {
    namespace = Config.artifactId
    configureAndroidLibrary(this)
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    // means androidDebugImplementation
    debugImplementation(projects.debugger.debuggerPlugin)
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

val generateBuildConfig by tasks.registering(Sync::class) {
    from(
        resources.text.fromString(BuildConfig)
    ) {
        rename { "BuildConfig.kt" }
        into(Config.namespace.replace(".", "/"))
    }
    // the target directory
    into(layout.buildDirectory.dir("generated/kotlin/src/commonMain"))
}

kotlin {
    sourceSets.commonMain {
        kotlin.srcDir(generateBuildConfig.map { it.destinationDir })
    }
}
