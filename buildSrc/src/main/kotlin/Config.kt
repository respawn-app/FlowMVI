@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "MissingPackageDeclaration",
    "UndocumentedPublicClass",
    "UndocumentedPublicProperty"
)

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Config {

    const val group = "pro.respawn"
    const val artifact = "kmmutils"

    const val artifactId = "$group.$artifact"

    const val majorRelease = 1
    const val minorRelease = 0
    const val patch = 1
    const val versionName = "$majorRelease.$minorRelease.$patch"

    // kotlin
    const val languageVersion = "1.8"
    val kotlinCompilerArgs = listOf(
        "-Xjvm-default=all", // enable all jvm optimizations
        "-Xcontext-receivers",
        "-Xbackend-threads=0", // parallel IR compilation
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlin.Experimental",
        "-opt-in=kotlin.RequiresOptIn",
        "-Xuse-k2",
        // "-XXLanguage:+ExplicitBackingFields"
    )

    val jvmTarget = JvmTarget.JVM_11
    val javaVersion = JavaVersion.VERSION_11
    const val compileSdk = 33
    const val targetSdk = compileSdk
    const val minSdk = 26
    const val kotlinVersion = "1.8"

    // android
    const val namespace = artifactId
    const val buildToolsVersion = "33.0.0"
    const val testRunner = "androidx.test.runner.AndroidJUnitRunner"
    const val isMinifyEnabledRelease = true
    const val isMinifyEnabledDebug = false
    const val defaultProguardFile = "proguard-android-optimize.txt"
    const val proguardFile = "proguard-rules.pro"
    const val consumerProguardFile = "consumer-rules.pro"

    val stabilityLevels = listOf("preview", "eap", "alpha", "beta", "m", "cr", "rc")
    object Detekt {

        const val configFile = "detekt.yml"
        val includedFiles = listOf("**/*.kt", "**/*.kts")
        val excludedFiles = listOf("**/resources/**", "**/build/**", "**/.idea/**")
    }
}
