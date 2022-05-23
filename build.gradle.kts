plugins {
    `kotlin-dsl`
}

rootProject.group = "com.nek12.flowMVI"
rootProject.version = "0.2.1"

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath ("com.github.ben-manes:gradle-versions-plugin:${Versions.versionsPlugin}")
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

    apply(plugin = "com.github.ben-manes.versions")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
            )
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
