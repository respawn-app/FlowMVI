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

    const val majorRelease = 2
    const val minorRelease = 5
    const val patch = 0
    const val postfix = "-alpha06" // include dash (-)
    const val majorVersionName = "$majorRelease.$minorRelease.$patch"
    const val versionName = "$majorVersionName$postfix"
    const val url = "https://github.com/respawn-app/FlowMVI"
    const val licenseFile = "LICENSE.txt"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    const val scmUrl = "https://github.com/respawn-app/FlowMVI.git"
    const val description = """A Kotlin Multiplatform MVI library based on coroutines with a powerful plugin system"""
    const val supportEmail = "hello@respawn.pro"
    const val vendorName = "Respawn Open Source Team"
    const val vendorId = "respawn-app"

    object Debugger {

        const val namespace = "${Config.namespace}.debugger"
        const val appDescription = "A debugger tool for FlowMVI - $description"
        const val name = "FlowMVI Debugger"
        const val appId = "fd36c0cc-ae50-4aad-8579-f37e1e8af99c"
    }

    object Sample {

        const val namespace = "${Config.namespace}.sample"
        const val appDescription = "Sample app for FlowMVI - $description"
        const val name = "FlowMVI Sample"
        const val appId = "a7f6783f-2bb5-433d-9e5c-9f608ddd42d5"
    }
    // kotlin

    val optIns = listOf(
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlinx.coroutines.FlowPreview",
        "kotlin.RequiresOptIn",
        "kotlin.experimental.ExperimentalTypeInference",
        "kotlin.contracts.ExperimentalContracts"
    )
    val compilerArgs = listOf(
        "-Xbackend-threads=0", // parallel IR compilation
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:experimentalStrongSkipping=true"
    )
    val jvmCompilerArgs = buildList {
        addAll(compilerArgs)
        add("-Xjvm-default=all") // enable all jvm optimizations
        add("-Xcontext-receivers")
        add("-Xstring-concat=inline")
        addAll(optIns.map { "-opt-in=$it" })
    }

    val jvmTarget = JvmTarget.JVM_11
    val idePluginJvmTarget = JvmTarget.JVM_17
    val javaVersion = JavaVersion.VERSION_11
    val kotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
    const val compileSdk = 34
    const val targetSdk = compileSdk
    const val minSdk = 21
    const val appMinSdk = 26
    const val publishingVariant = "release"

    // android
    const val namespace = artifactId
    const val testRunner = "androidx.test.runner.AndroidJUnitRunner"
    const val isMinifyEnabledRelease = false
    const val isMinifyEnabledDebug = false
    const val defaultProguardFile = "proguard-android-optimize.txt"
    const val proguardFile = "proguard-rules.pro"
    const val consumerProguardFile = "consumer-rules.pro"

    // position reflects the level of stability, order is important
    val stabilityLevels = listOf("snapshot", "eap", "preview", "alpha", "beta", "m", "cr", "rc")
    val minStabilityLevel = stabilityLevels.indexOf("beta")

    object Detekt {

        const val configFile = "detekt.yml"
        val includedFiles = listOf("**/*.kt", "**/*.kts")
        val excludedFiles = listOf("**/resources/**", "**/build/**", "**/.idea/**")
    }
}
