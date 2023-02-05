@file:Suppress("MissingPackageDeclaration")

import gradle.kotlin.dsl.accessors._23673d25a43a3ae0349f048e5ad21ead.commonMain
import gradle.kotlin.dsl.accessors._23673d25a43a3ae0349f048e5ad21ead.commonTest
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@Suppress("unused", "UNUSED_VARIABLE", "UndocumentedPublicFunction")
fun Project.configureMultiplatform(
    ext: KotlinMultiplatformExtension,
    android: Boolean = false,
    ios: Boolean = false,
    jvm: Boolean = false,
    js: Boolean = false,
    linux: Boolean = false,
    mingw: Boolean = false,
) = ext.apply {

    explicitApi()

    val libs by versionCatalog

    val commonMain by sourceSets.getting
    val commonTest by sourceSets.getting {
        dependencies {
            implementation(kotlin("test"))
        }
    }

    sourceSets.apply {
        all {
            languageSettings {
                languageVersion = Config.kotlinVersion
                progressiveMode = true
                optIn("kotlin.RequiresOptIn")
            }
        }
    }

    if (linux) {
        linuxX64()
    }

    if (mingw) {
        mingwX64()
    }

    if (js) {
        js(IR) {
            browser()
            nodejs()
        }
    }

    if (android) {
        android {
            publishAllLibraryVariants()
        }

        sourceSets.apply {
            val androidMain by getting
            // val androidTest by getting
        }
    }

    if (jvm) {
        jvm {
            jvmToolchain(Config.jvmTarget.target.toInt())
            compilations.all {
                kotlinOptions.jvmTarget = Config.jvmTarget.target
            }
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }

        sourceSets.apply {
            val jvmTest by getting {
                dependencies {
                    implementation(libs.requireLib("kotest-junit"))
                }
            }
        }
    }
    if (ios) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
            macosArm64(),
            macosX64(),
        ).forEach {
            it.binaries.framework {
                binaryOption("bundleId", Config.artifactId)
                binaryOption("bundleVersion", Config.versionName)
                baseName = Config.artifactId
            }
        }
        sourceSets.apply {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }
            val iosX64Test by getting
            val iosArm64Test by getting
            val iosSimulatorArm64Test by getting
            val iosTest by creating {
                dependsOn(commonTest)
                iosX64Test.dependsOn(this)
                iosArm64Test.dependsOn(this)
                iosSimulatorArm64Test.dependsOn(this)
            }
        }
    } // ios
}
