@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "MissingPackageDeclaration",
    "UndocumentedPublicClass",
    "UndocumentedPublicProperty",
    "MaxLineLength"
)

import org.gradle.api.JavaVersion
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Config {

    const val group = "pro.respawn"
    const val artifact = "flowmvi"

    const val artifactId = "$group.$artifact"

    const val majorRelease = 3
    const val minorRelease = 2
    const val patch = 0
    const val postfix = "-alpha03" // include dash (-)
    const val versionCode = 10

    const val majorVersionName = "$majorRelease.$minorRelease.$patch"
    const val versionName = "$majorVersionName$postfix"
    const val url = "https://github.com/respawn-app/FlowMVI"
    const val developerUrl = "https://respawn.pro"
    const val licenseFile = "LICENSE.txt"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    const val scmUrl = "https://github.com/respawn-app/FlowMVI.git"
    const val docsUrl = "https://opensource.respawn.pro/FlowMVI/"
    const val description =
        """A Kotlin Multiplatform architecture framework based on coroutines with a powerful plugin system."""
    const val supportEmail = "hello@respawn.pro"
    const val vendorName = "Respawn Open Source Team"
    const val vendorId = "respawn-app"
    const val name = "FlowMVI"

    val jvmTarget = JvmTarget.JVM_11
    val javaVersion = JavaVersion.VERSION_11
    val optIns = listOf(
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlinx.coroutines.FlowPreview",
        "kotlin.RequiresOptIn",
        "kotlin.experimental.ExperimentalTypeInference",
        "kotlin.uuid.ExperimentalUuidApi",
        "kotlin.contracts.ExperimentalContracts",
        "org.jetbrains.compose.resources.ExperimentalResourceApi"
    )
    val compilerArgs = listOf(
        "-Xbackend-threads=0", // parallel IR compilation
        "-Xexpect-actual-classes",
        "-Xwasm-use-new-exception-proposal",
        "-Xconsistent-data-class-copy-visibility",
        "-Xsuppress-warning=NOTHING_TO_INLINE",
        "-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER",
        "-Xwasm-debugger-custom-formatters"
    )
    val jvmCompilerArgs = buildList {
        addAll(compilerArgs)
        add("-Xjvm-default=all") // enable all jvm optimizations
        add("-Xcontext-receivers")
        add("-Xstring-concat=inline")
        add("-Xlambdas=indy")
        add("-Xjdk-release=${jvmTarget.target}")
    }

    // android
    const val compileSdk = 36
    const val targetSdk = compileSdk
    const val minSdk = 21
    const val appMinSdk = 26
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

    // region Plugin
    object Plugin {

        const val id = "$artifactId.ideplugin"
        const val name = Config.name
        const val minIdeaVersion = "241"
        const val certPath = "certificates/plugin_certificate_chain.crt"
        val jvmTarget = JvmTarget.JVM_17

        @Language("HTML")
        const val description = """
IDE Plugin for FlowMVI - ${Config.description}
<br/>
This plugin aids in development with the library using:
<ul>
  <li>An integrated debugger tool window with time travel and logging.</li>
  <li>Live templates for creating stores, models, screens, plugins, and more.</li>
  <li>Manipulating the store remotely.</li>
  <li>Additional lint and safety checks.</li>
</ul>
<br/>
Missing feature? Found a bug? Open an issue on <a href="$url">Github</a>.
<br/>
Learn how to start using the framework in the <a href="$docsUrl/quickstart">Quickstart</a> guide.
<br/>
To use the debugging feature of the plugin, you need to have your app configured correctly. Learn how to do this in the <a href="$docsUrl/debugging">Documentation</a>.
<br/>
Note - the plugin's version (latest is $versionName) is synced with the version of the library it expects.
If you are using a severely outdated version of either library or the plugin, you may run into issues.
"""
    }

    // endregion
}
