plugins {
    kotlin("multiplatform")
    alias(libs.plugins.serialization)
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    configureMultiplatform(this) {
        common {
            group("nonBrowser") {
                withJvm()
                withNative()
                withAndroidTarget()
            }
            group("browser") {
                withWasm()
                withJs()
            }
        }
    }

    sourceSets {
        nativeMain.dependencies {
            implementation(libs.kotlin.io)
        }
        androidMain.dependencies {
            api(libs.lifecycle.savedstate)
        }
        commonMain.dependencies {
            api(projects.core)
            api(libs.kotlin.serialization.json)
            implementation(libs.kotlin.atomicfu)
        }
    }
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.savedstate"
}

publishMultiplatform()
