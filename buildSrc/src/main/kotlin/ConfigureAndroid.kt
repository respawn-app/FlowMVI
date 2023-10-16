@file:Suppress("MemberVisibilityCanBePrivate", "MissingPackageDeclaration", "UndocumentedPublicFunction")

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import gradle.kotlin.dsl.accessors._3b641d770f24f39f98303fbbdf1a8d7e.kotlin
import gradle.kotlin.dsl.accessors._49c46f32d5be065ecf6a6309cc175bfd.kotlinOptions
import org.gradle.api.Project

fun Project.configureAndroid(
    commonExtension: CommonExtension<*, *, *, *, *>,
) = commonExtension.apply {
    compileSdk = Config.compileSdk
    val libs by versionCatalog

    defaultConfig {
        minSdk = Config.minSdk
        testInstrumentationRunner = Config.testRunner
        proguardFiles(getDefaultProguardFile(Config.defaultProguardFile), Config.proguardFile)
    }

    lint {
        warning += "AutoboxingStateCreation"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = Config.isMinifyEnabledRelease
        }
        getByName("debug") {
            isMinifyEnabled = Config.isMinifyEnabledDebug
        }
    }

    compileOptions {
        sourceCompatibility = Config.javaVersion
        targetCompatibility = Config.javaVersion
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        prefab = false
        renderScript = false
        resValues = false
        shaders = false
        viewBinding = false
        compose = false
    }

    packaging {
        resources {
            excludes += setOf(
                "DebugProbesKt.bin",
                "/META-INF/{AL2.0,LGPL2.1}",
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.apply {
                    maxHeapSize = "1G"
                    forkEvery = 100
                    jvmArgs = listOf("-Xmx1g", "-Xms512m")
                }
            }
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.requireVersion("compose-compiler")
        useLiveLiterals = true
    }
}

fun Project.configureAndroidLibrary(variant: LibraryExtension) = variant.apply {
    configureAndroid(this)

    testFixtures {
        enable = true
    }

    defaultConfig {
        consumerProguardFiles(file(Config.consumerProguardFile))
    }

    buildTypes {
        release {
            setProperty(
                "archivesBaseName",
                project.name
            )
        }
    }

    libraryVariants.all {
        sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
