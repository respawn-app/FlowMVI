enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
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
dependencyResolutionManagement {
    // REQUIRED for IDE module configuration to resolve IDE platform
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    versionCatalogs {
        create("applibs") {
            from(files("sample/libs.versions.toml"))
        }
    }
}

rootProject.name = "FlowMVI"

include(":sample")
include(":sample:androidApp")
include(":sample:wasmApp")
include(":test")
include(":core")
include(":android")
include(":compose")
include(":savedstate")
include(":essenty")
include(":essenty:essenty-compose")
include(":debugger:app")
include(":debugger:debugger-client")
include(":debugger:debugger-plugin")
include(":debugger:server")
include(":debugger:debugger-common")
// include(":debugger:ideplugin")
