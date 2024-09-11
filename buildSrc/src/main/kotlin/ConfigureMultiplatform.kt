@file:Suppress("MissingPackageDeclaration", "unused", "UndocumentedPublicFunction", "LongMethod", "UnusedImports")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
fun Project.configureMultiplatform(
    ext: KotlinMultiplatformExtension,
    jvm: Boolean = true,
    android: Boolean = true,
    linux: Boolean = true,
    iOs: Boolean = true,
    js: Boolean = true,
    tvOs: Boolean = true,
    macOs: Boolean = true,
    watchOs: Boolean = true,
    windows: Boolean = true,
    wasmJs: Boolean = true,
    wasmWasi: Boolean = false, // TODO: Coroutines do not support wasmWasi yet
    configure: KotlinHierarchyBuilder.Root.() -> Unit = {},
) = ext.apply {
    val libs by versionCatalog
    explicitApi()
    applyDefaultHierarchyTemplate(configure)
    withSourcesJar(true)

    if (linux) {
        linuxX64()
        linuxArm64()
    }

    if (windows) mingwX64()

    if (js) js(IR) {
        browser()
        nodejs()
        binaries.library()
    }

    if (wasmJs) wasmJs {
        moduleName = this@configureMultiplatform.name
        nodejs()
        browser()
        binaries.library()
    }

    if (wasmWasi) wasmWasi()

    if (android) androidTarget {
        publishLibraryVariants("release")
    }.compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                jvmTarget.set(Config.jvmTarget)
                freeCompilerArgs.addAll(Config.jvmCompilerArgs)
            }
        }
    }

    if (jvm) jvm().compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                jvmTarget.set(Config.jvmTarget)
                freeCompilerArgs.addAll(Config.jvmCompilerArgs)
            }
        }
    }

    sequence {
        if (iOs) {
            yield(iosX64())
            yield(iosArm64())
            yield(iosSimulatorArm64())
        }
        if (macOs) {
            yield(macosArm64())
            yield(macosX64())
        }
        if (tvOs) {
            yield(tvosX64())
            yield(tvosArm64())
            yield(tvosSimulatorArm64())
        }
        if (watchOs) {
            yield(watchosX64())
            yield(watchosArm64())
            yield(watchosDeviceArm64())
            yield(watchosSimulatorArm64())
        }
    }.toList() // for now, do nothing, but iterate the lazy sequence

    sourceSets.apply {
        if (jvm) {
            val jvmTest by getting {
                dependencies {
                    implementation(libs.requireLib("kotest-junit"))
                }
            }
        }
        all {
            languageSettings {
                progressiveMode = true
                Config.optIns.forEach { optIn(it) }
            }
        }
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll(Config.compilerArgs)
                    optIn.addAll(Config.optIns)
                    progressiveMode.set(true)
                }
            }
        }
    }
}
