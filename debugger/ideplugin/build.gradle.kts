import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.intellij.ide)
    kotlin("jvm")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}
val props by localProperties

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
}

intellijPlatform {
    projectName = Config.debuggerName
    verifyPlugin {
        ides {
            recommended()
        }
    }
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
            sinceBuild = "233"
            untilBuild = "241.*"
        }
        vendor {
            name = Config.vendorName
            email = Config.supportEmail
            url = Config.url
        }
        changeNotes
        id = "${Config.artifactId}.ideplugin"
        description = Config.debuggerPluginDescription
        name = Config.debuggerName
        version = Config.versionName
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = Config.idePluginJvmTarget.target
        targetCompatibility = Config.idePluginJvmTarget.target
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = Config.idePluginJvmTarget.target
    }
}

dependencies {
    compileOnly(compose.desktop.currentOs)
    implementation(projects.core)
    intellijPlatform {
        plugin("org.jetbrains.compose.intellij.platform")
        // bundledPlugin("org.jetbrains.kotlin")
        // props["plugin.local.ide.path"]?.toString()?.let(::local)
        intellijIdeaCommunity("2023.3.3")
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
}

tasks {
// needed when plugin provides custom settings exposed to the UI
    buildSearchableOptions {
        enabled = false
    }
}
