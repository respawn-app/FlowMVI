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
    const val artifact = "flowmvi"

    const val artifactId = "$group.$artifact"

    const val majorRelease = 1
    const val minorRelease = 1
    const val patch = 4
    const val versionName = "$majorRelease.$minorRelease.$patch"

    // kotlin

    val optIns = listOf(
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlinx.coroutines.FlowPreview",
        "kotlin.RequiresOptIn",
        "kotlin.experimental.ExperimentalTypeInference"
    )
    val kotlinCompilerArgs = listOf(
        "-Xjvm-default=all", // enable all jvm optimizations
        "-Xcontext-receivers",
        "-Xbackend-threads=0", // parallel IR compilation
        "-Xuse-k2",
        // "-XXLanguage:+ExplicitBackingFields"
    ) + optIns.map { "-opt-in=$it" }

    val jvmTarget = JvmTarget.JVM_11
    val javaVersion = JavaVersion.VERSION_11
    val kotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8
    const val compileSdk = 33
    const val targetSdk = compileSdk
    const val minSdk = 21
    const val appMinSdk = 26

    // android
    const val namespace = artifactId
    const val buildToolsVersion = "33.0.0"
    const val testRunner = "androidx.test.runner.AndroidJUnitRunner"
    const val isMinifyEnabledRelease = false
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
