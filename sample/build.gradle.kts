import org.intellij.lang.annotations.Language
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id(libs.plugins.kotlin.multiplatform.id)
    id(applibs.plugins.android.application.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

// region buildconfig
@Language("Kotlin")
// language=kotlin
val BuildConfig = """
    package ${Config.Sample.namespace}

    internal object BuildFlags {
        const val VersionName = "${Config.versionName}"
        const val ProjectDescription = ${ "\"\"\"" + Config.Sample.appDescription + "\"\"\""}
        const val PrivacyUrl = "${Config.Sample.privacyUrl}"
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
// endregion

kotlin {
    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "app"
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
                export = true
            }
            testTask { enabled = false }
        }
        compilerOptions {
            freeCompilerArgs.addAll(Config.wasmCompilerArgs)
        }
    }
    jvm("desktop")

    androidTarget().compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                jvmTarget = Config.jvmTarget
                freeCompilerArgs.addAll(Config.jvmCompilerArgs)
            }
        }
    }

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
                Config.optIns.forEach { optIn(it) }
            }
        }
        commonMain {
            kotlin.srcDir(generateBuildConfig.map { it.destinationDir })
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.animation)
                implementation(libs.compose.animation.graphics)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.ui.util)

                implementation(libs.bundles.serialization)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.io)

                implementation(applibs.bundles.kmputils)
                implementation(applibs.bundles.koin)
                implementation(applibs.apiresult)
                implementation(applibs.decompose.compose)
                implementation(applibs.compose.codehighlighting)
                implementation(applibs.decompose)

                implementation(projects.core)
                implementation(projects.essenty.essentyCompose)
                implementation(projects.essenty)
                implementation(projects.compose)
                implementation(projects.savedstate)
                implementation(projects.metrics)
            }
        }
        nativeMain.dependencies {
            implementation(projects.debugger.debuggerPlugin)
        }
        desktopMain.dependencies {
            implementation(projects.debugger.debuggerPlugin)
            implementation(libs.kotlin.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }
        androidMain.dependencies {
            api(libs.compose.ui.tooling.preview)
            implementation(projects.android)
            implementation(applibs.view.constraintlayout)
            implementation(applibs.view.material)
            implementation(applibs.koin.android)

            implementation(applibs.androidx.splashscreen)

            implementation(libs.compose.activity)
            implementation(applibs.koin.android)
            implementation(applibs.koin.android.compose)
            implementation(applibs.view.material)
        }
        wasmJsMain.dependencies {
            implementation(libs.essenty.statekeeper)
        }
    } // sets
}
android {
    namespace = Config.Sample.namespace
    configureAndroid()
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
    defaultConfig {
        compileSdk = Config.compileSdk
        applicationId = Config.artifactId
        minSdk = Config.appMinSdk
        targetSdk = Config.targetSdk
        versionCode = Config.versionCode
        versionName = Config.versionName
    }
    applicationVariants.all {
        outputs
            .matching { "apk" in it.outputFile.extension }
            .all {
                this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                outputFileName = "${Config.Sample.namespace}.apk"
            }
    }
    val props by localProperties()
    val passwd = props["keystore.password"]?.toString()?.trim()?.takeIf { it.isNotBlank() }
    val keystore = File(rootDir, "certificates/keystore.jks")
    val signed = passwd != null && keystore.exists()
    if (signed) signingConfigs {
        create("release") {
            keyAlias = "key"
            keyPassword = passwd
            storeFile = keystore
            storePassword = passwd
        }
    } else println("w: skipping signing because keystore.password property is not present or keystore is missing")
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "-debug"
            isShrinkResources = Config.isMinifyEnabledDebug
        }
        release {
            ndk.debugSymbolLevel = "FULL"
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = if (signed) signingConfigs.getByName("release") else null
        }
    }
    androidResources {
        generateLocaleConfig = false
    }
}

dependencies {
    // means androidDebugImplementation
    debugImplementation(projects.debugger.debuggerPlugin)
}
compose {
    resources {
        packageOfResClass = Config.Sample.namespace
        publicResClass = false
    }
    android { }
    web { }
    desktop {
        application {
            mainClass = "${Config.Sample.namespace}.MainKt"
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
}
