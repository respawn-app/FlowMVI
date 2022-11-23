@file:Suppress("MissingPackageDeclaration")

import org.gradle.api.JavaVersion

object Config {
    val javaVersion = JavaVersion.VERSION_11
    const val jvmTarget = "11"
    const val compileSdk = 33
    const val targetSdk = compileSdk
    const val minSdk = 26
    const val group = "com.nek12.FlowMVI"
    const val version = "1.0.0-alpha02"

    val kotlinCompilerArgs = listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-Xjvm-default=all",
        "-Xbackend-threads=0", // parallel IR compilation
        "-opt-in=kotlin.Experimental",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlin.experimental.ExperimentalTypeInference",
        "-opt-in=kotlin.RequiresOptIn",
    )

    val stabilityLevels = listOf("preview", "eap", "alpha", "beta", "m", "cr", "rc")

    object Detekt {

        const val configFile = "detekt.yml"
        val includedFiles = listOf("**/*.kt") //  "**/*.kts"
        val excludedFiles = listOf("**/resources/**", "**/build/**", "**/.idea/**")
    }
}
