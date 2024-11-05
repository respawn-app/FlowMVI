@file:Suppress("UnstableApiUsage")

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

// https://youtrack.jetbrains.com/issue/IJPL-1901
configurations.implementation.configure {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
}
configurations.api.configure {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
}

dependencies {
    compileOnly(compose.desktop.currentOs)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.coroutines.core)

    // implementation(libs.kotlin.coroutines.swing)

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
    buildPlugin {
        archiveFileName = "flowmvi-$version.zip"
    }
}
