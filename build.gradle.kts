plugins {
    `kotlin-dsl`
}

rootProject.group = "com.nek12.flowMVI"
rootProject.version = "0.1.6"

buildscript {

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    }
}

allprojects {
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-P", //TODO: Remove once support 1.6.21
                "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
            )
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    when (name) {
        "app" -> apply(plugin = "com.android.application")
        "core" -> apply(plugin = "java-library")
        else -> apply(plugin = "android-library")
    }

}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
