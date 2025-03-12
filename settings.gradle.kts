enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    // REQUIRED for IDE module configuration to resolve IDE platform
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        // mavenLocal()
        // maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        google()
        mavenCentral()
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
include(":metrics")
include(":benchmarks")
include(":essenty")
include(":essenty:essenty-compose")
include(":debugger:app")
include(":debugger:debugger-client")
include(":debugger:debugger-plugin")
include(":debugger:server")
include(":debugger:debugger-common")
include(":debugger:ideplugin")
