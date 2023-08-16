import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.signing

plugins {
    // id(libs.plugins.atomicfu.id)
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    configureMultiplatform(this)
}

android {
    configureAndroidLibrary(this)
}

publishMultiplatform()
