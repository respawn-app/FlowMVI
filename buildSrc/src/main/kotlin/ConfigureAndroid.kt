@file:Suppress("MemberVisibilityCanBePrivate", "MissingPackageDeclaration", "UndocumentedPublicFunction")

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

fun Project.configureAndroid(
    commonExtension: CommonExtension<*, *, *, *>,
) = commonExtension.apply {
    compileSdk = Config.compileSdk
    buildToolsVersion = Config.buildToolsVersion

    defaultConfig {
        minSdk = Config.minSdk
        testInstrumentationRunner = Config.testRunner
        proguardFiles(getDefaultProguardFile(Config.defaultProguardFile), Config.proguardFile)
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.apply {
                    useJUnitPlatform()
                    maxHeapSize = "1G"
                    setForkEvery(100)
                    jvmArgs = listOf("-Xmx1g", "-Xms512m")
                }
            }
        }
    }
}

fun Project.configureAndroidLibrary(variant: LibraryExtension) = variant.apply {
    configureAndroid(this)

    buildTypes {
        release {
            setProperty(
                "archivesBaseName",
                project.name
            )
            isMinifyEnabled = Config.isMinifyEnabledRelease
        }
    }

    defaultConfig {
        consumerProguardFiles(file(Config.consumerProguardFile))
    }

    buildFeatures {
        buildConfig = false
        androidResources = true // required for R8 to work
    }

    libraryVariants.all {
        sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
