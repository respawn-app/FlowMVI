@file:Suppress("MemberVisibilityCanBePrivate", "MissingPackageDeclaration", "UndocumentedPublicFunction")

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

fun Project.configureAndroid(
    commonExtension: CommonExtension<*, *, *, *, *>,
) = commonExtension.apply {
    compileSdk = Config.compileSdk

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

    kotlinOptions {
        freeCompilerArgs += Config.jvmCompilerArgs
        jvmTarget = Config.jvmTarget.target
        languageVersion = Config.kotlinVersion.version
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

    val libs by versionCatalog
    composeOptions {
        kotlinCompilerExtensionVersion = libs.requireVersion("compose-compiler")
    }

    packaging {
        resources {
            excludes += setOf(
                "DebugProbesKt.bin",
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/versions/9/previous-compilation-data.bin"
            )
        }
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

    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }

    buildTypes {
        release {
            setProperty(
                "archivesBaseName",
                project.name
            )
        }
    }

    defaultConfig {
        consumerProguardFiles(file(Config.consumerProguardFile))
    }

    libraryVariants.all {
        sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
