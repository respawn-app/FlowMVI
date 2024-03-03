enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    // TODO: https://github.com/Kotlin/kotlinx-atomicfu/issues/56
    resolutionStrategy {
        eachPlugin {
            val module = when (requested.id.id) {
                "kotlinx-atomicfu" -> "org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}"
                else -> null
            }
            if (module != null) {
                useModule(module)
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}
dependencyResolutionManagement {
    // REQUIRED for IDE module configuration to resolve IDE platform
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        google()
        // kmm plugin adds "ivy" repo as part of the apply block
        ivyNative()
        node()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "FlowMVI"

include(":app")
include(":test")
include(":core")
include(":android")
include(":android-compose")
include(":android-view")
include(":compose")
include(":savedstate")
include(":debugger:app")
include(":debugger:client")
include(":debugger:plugin")
include(":debugger:server")
include(":debugger:common")
// include(":debugger:ideplugin")

fun RepositoryHandler.node() {
    exclusiveContent {
        forRepository {
            ivy("https://nodejs.org/dist/") {
                name = "Node Distributions at $url"
                patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                metadataSources { artifact() }
                content { includeModule("org.nodejs", "node") }
            }
        }
        filter { includeGroup("org.nodejs") }
    }

    exclusiveContent {
        forRepository {
            ivy("https://github.com/yarnpkg/yarn/releases/download") {
                name = "Yarn Distributions at $url"
                patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
                metadataSources { artifact() }
                content { includeModule("com.yarnpkg", "yarn") }
            }
        }
        filter { includeGroup("com.yarnpkg") }
    }
}

fun RepositoryHandler.ivyNative() {
    ivy { url = uri("https://download.jetbrains.com") }

    // TODO: Maybe this is not needed anymore
    exclusiveContent {
        forRepository {
            this@ivyNative.ivy("https://download.jetbrains.com/kotlin/native/builds") {
                name = "Kotlin Native"
                patternLayout {
                    listOf(
                        "macos-x86_64",
                        "macos-aarch64",
                        "osx-x86_64",
                        "osx-aarch64",
                        "linux-x86_64",
                        "windows-x86_64",
                    ).forEach { os ->
                        listOf("dev", "releases").forEach { stage ->
                            artifact("$stage/[revision]/$os/[artifact]-[revision].[ext]")
                        }
                    }
                }
                metadataSources { artifact() }
            }
        }
        filter { includeModuleByRegex(".*", ".*kotlin-native-prebuilt.*") }
    }
}
