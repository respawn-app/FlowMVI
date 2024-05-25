@file:Suppress("MemberVisibilityCanBePrivate", "MissingPackageDeclaration", "UndocumentedPublicFunction")

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

fun CommonExtension<*, *, *, *, *, *>.configureAndroid() = apply {
    compileSdk = Config.compileSdk

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
        resources.excludes += listOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "DebugProbesKt.bin",
            "META-INF/versions/9/previous-compilation-data.bin"
        )
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
}

fun Project.configureAndroidLibrary(variant: LibraryExtension) = variant.apply {
    configureAndroid()

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
