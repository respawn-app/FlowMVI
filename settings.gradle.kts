pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
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

buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // kmm plugin adds "ivy" repo as part of the apply block
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        ivyNative()
        mavenCentral()
    }
}

rootProject.name = "pro.respawn.flowmvi"

include(":app")
include(":core")
include(":android")
include(":android-compose")
include(":android-view")


fun RepositoryHandler.ivyNative() {
    ivy { url = uri("https://download.jetbrains.com") }

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
