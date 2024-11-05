@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.intellij.ide)
}

val props by localProperties()

repositories {
    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
}

intellijPlatform {
    projectName = Config.name
    // needed when plugin provides custom settings exposed to the UI
    buildSearchableOptions = false
    signing {
        certificateChainFile = File("plugin_certificate_chain.crt")
        privateKey = props["plugin.publishing.privatekey"]?.toString()
        password = props["signing.password"]?.toString()
    }
    publishing {
        token = props["plugin.publishing.token"]?.toString()
    }
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
        vendor {
            name = Config.vendorName
            email = Config.supportEmail
            url = Config.url
        }
        id = "${Config.artifactId}.ideplugin"
        description = Config.Debugger.appDescription
        name = Config.Debugger.name
        version = Config.versionName
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(Config.idePluginJvmTarget)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = Config.idePluginJvmTarget.target
        targetCompatibility = Config.idePluginJvmTarget.target
    }
}

dependencies {
    compileOnly(compose.desktop.currentOs)
    compileOnly(libs.kotlin.stdlib)
    implementation(compose.desktop.common)
    implementation(compose.desktop.linux_arm64)
    implementation(compose.desktop.linux_x64)
    implementation(compose.desktop.macos_arm64)
    implementation(compose.desktop.macos_x64)
    implementation(compose.desktop.windows_x64)

    implementation(projects.core)
    implementation(projects.debugger.server)

    implementation(applibs.decompose)
    implementation(applibs.decompose.compose)
    implementation(applibs.bundles.koin)

    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij.idea)
        pluginVerifier()
        zipSigner()
        instrumentationTools()
        bundledPlugin(libs.kotlin.stdlib.map(Dependency::getGroup))
    }
}

tasks {
    // workaround for https://youtrack.jetbrains.com/issue/IDEA-285839/Classpath-clash-when-using-coroutines-in-an-unbundled-IntelliJ-plugin
    buildPlugin {
        exclude { "coroutines" in it.name }
        archiveFileName = "flowmvi-$version.zip"
    }
    prepareSandbox {
        exclude { "coroutines" in it.name }
    }
}
//
// configurations.configureEach {
//     resolutionStrategy.eachDependency {
//         if (requested.group == libs.kotlin.stdlib.get().group) {
//             useVersion(libs.versions.kotlin.asProvider().get())
//         }
//     }
// }
