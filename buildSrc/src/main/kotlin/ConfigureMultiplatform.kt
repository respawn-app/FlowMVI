@file:Suppress("MissingPackageDeclaration", "unused", "UndocumentedPublicFunction", "LongMethod")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureMultiplatform(
    ext: KotlinMultiplatformExtension,
    jvm: Boolean = true,
    android: Boolean = true,
    linux: Boolean = true,
    iOs: Boolean = true,
    js: Boolean = true,
    tvOs: Boolean = true,
    macOs: Boolean = true,
    watchOs: Boolean = true
) = ext.apply {
    val libs by versionCatalog
    explicitApi()
    applyDefaultHierarchyTemplate()
    withSourcesJar(true)

    if (linux) {
        linuxX64()
        linuxArm64()
        mingwX64()
    }

    if (js) {
        js(IR) {
            browser()
            nodejs()
            binaries.library()
        }
    }

    if (android) {
        androidTarget {
            publishAllLibraryVariants()
        }
    }

    if (jvm) {
        jvm()
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
    }.forEach {
        it.binaries.framework {
            binaryOption("bundleId", Config.artifactId)
            binaryOption("bundleVersion", Config.versionName)
            baseName = Config.artifactId
        }
    }

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
                languageVersion = Config.kotlinVersion.version
                Config.optIns.forEach { optIn(it) }
            }
        }
    }
}
