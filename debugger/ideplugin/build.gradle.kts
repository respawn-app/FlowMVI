@file:Suppress("UnstableApiUsage")

import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import kotlin.text.replace

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
        certificateChainFile = rootProject.rootDir.resolve(Config.Plugin.certPath)
        privateKey = props["plugin.publishing.privatekey"]?.toString()
        password = props["signing.password"]?.toString()
    }
    publishing {
        token = props["plugin.publishing.token"]?.toString()
        hidden = true
    }
    pluginVerification {
        ides {
            // TODO: https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/1965
            // props["plugin.local.ide.path"]?.toString()?.let(::local)
            create(
                IntelliJPlatformType.IntellijIdeaCommunity,
                libs.versions.intellij.idea.get()
            )
        }
    }
    pluginConfiguration {
        ideaVersion {
            sinceBuild = Config.Plugin.minIdeaVersion
            untilBuild = provider { null }
        }
        vendor {
            name = Config.vendorName
            email = Config.supportEmail
            url = Config.url
        }
        id = Config.Plugin.id
        description = Config.Plugin.description
        name = Config.Plugin.name
        version = Config.versionName
        changeNotes = System.getenv("CHANGELOG")?.replace("\n", "<br>")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(Config.Plugin.jvmTarget)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = Config.Plugin.jvmTarget.target
        targetCompatibility = Config.Plugin.jvmTarget.target
    }
    buildPlugin {
        archiveFileName = "${Config.artifact}-$version.zip"
    }
}

// https://youtrack.jetbrains.com/issue/IJPL-1901
configurations {
    implementation.configure {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
    }
    api.configure {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
    }
}

dependencies {
    compileOnly(compose.desktop.currentOs)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.coroutines.core)

    // platform already includes bindings
    // implementation(libs.kotlin.coroutines.swing)

    implementation(compose.desktop.common)
    implementation(compose.desktop.linux_arm64)
    implementation(compose.desktop.linux_x64)
    implementation(compose.desktop.macos_arm64)
    implementation(compose.desktop.macos_x64)
    implementation(compose.desktop.windows_x64)
    implementation(compose.material3)

    implementation(projects.core)
    implementation(projects.debugger.server)

    implementation(applibs.decompose)
    implementation(applibs.decompose.compose)
    implementation(applibs.bundles.koin)

    intellijPlatform {
        @Suppress("DEPRECATION") // crashes without this usage :D
        intellijIdeaCommunity(libs.versions.intellij.idea)
        pluginVerifier()
        zipSigner()
        bundledPlugin(libs.kotlin.stdlib.map(Dependency::getGroup))
    }
}
