@file:Suppress("MissingPackageDeclaration", "unused", "UNUSED_VARIABLE", "UndocumentedPublicFunction", "LongMethod")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.configureMultiplatform(
    ext: KotlinMultiplatformExtension,
) = ext.apply {
    val libs by versionCatalog
    explicitApi()

    targetHierarchy.default()

    linuxX64()
    linuxArm64()
    mingwX64()

    js(IR) {
        browser()
        nodejs()
        binaries.library()
        binaries.executable()
    }
    // TODO: KMM js <> gradle 8.0 incompatibility
    tasks.run {
        val jsLibrary = named("jsProductionLibraryCompileSync")
        val jsExecutable = named("jsProductionExecutableCompileSync")
        named("jsBrowserProductionWebpack").configure {
            dependsOn(jsLibrary)
        }
        named("jsBrowserProductionLibraryPrepare").configure {
            dependsOn(jsExecutable)
        }
        named("jsNodeProductionLibraryPrepare").configure {
            dependsOn(jsExecutable)
        }
    }

    androidTarget {
        publishAllLibraryVariants()
    }

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = Config.jvmTarget.target
                freeCompilerArgs += Config.jvmCompilerArgs
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
        macosX64(),
        tvosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        watchosX64(),
        watchosArm64(),
        watchosDeviceArm64(),
        watchosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            binaryOption("bundleId", Config.artifactId)
            binaryOption("bundleVersion", Config.versionName)
            baseName = Config.artifactId
        }
    }

    sourceSets.apply {
        val jvmTest by getting {
            dependencies {
                implementation(libs.requireLib("kotest-junit"))
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
