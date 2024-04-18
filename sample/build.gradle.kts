import org.intellij.lang.annotations.Language
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    id(libs.plugins.androidLibrary.id)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)
}

compose.resources {
    packageOfResClass = Config.Sample.namespace
    publicResClass = false
}

@Language("Kotlin")
// language=kotlin
val BuildConfig = """
    package ${Config.Sample.namespace}

    internal object BuildFlags {
        const val VersionName = "${Config.versionName}"
        const val ProjectDescription = "${Config.Sample.appDescription}"
    }
""".trimIndent()

val generateBuildConfig by tasks.registering(Sync::class) {
    from(resources.text.fromString(BuildConfig)) {
        rename { "BuildFlags.kt" }
        into(Config.Sample.namespace.replace(".", "/"))
    }
    // the target directory
    into(layout.buildDirectory.dir("generated/kotlin/src/commonMain"))
}

kotlin {
    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = Config.Sample.namespace
        nodejs()
        browser()
        binaries.library()
        applyBinaryen()
    }
    jvm("desktop")

    androidTarget()

    // sequence {
    //     yield(iosX64())
    //     yield(iosArm64())
    //     yield(iosSimulatorArm64())
    //     // yield(macosArm64())
    //     // yield(macosX64())
    // }.toList()

    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting

        all {
            languageSettings {
                progressiveMode = true
                languageVersion = Config.kotlinVersion.version
                Config.optIns.forEach { optIn(it) }
            }
        }
        commonMain {
            kotlin.srcDir(generateBuildConfig.map { it.destinationDir })
            dependencies {
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
                // implementation(applibs.compose.codehighlighting)
                implementation(applibs.decompose)

                implementation(projects.core)
                implementation(projects.essenty.essentyCompose)
                implementation(projects.essenty)
                implementation(projects.compose)
                implementation(projects.savedstate)
            }
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
            implementation(projects.android)
            implementation(applibs.view.constraintlayout)
            implementation(applibs.view.material)
            implementation(applibs.koin.android)
        }
        wasmJsMain.dependencies {
            implementation(applibs.okio.fakefsys)
        }
    } // sets
}
android {
    namespace = Config.Sample.namespace
    configureAndroidLibrary(this)
    buildFeatures {
        viewBinding = true
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
