pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
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
        mavenCentral()
        ivy { url = uri("https://download.jetbrains.com") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "com.nek12.flowMVI"

include(":app")
include(":core")
include(":android")
include(":android-compose")
include(":android-view")
