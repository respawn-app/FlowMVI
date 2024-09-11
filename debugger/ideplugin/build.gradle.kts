import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.intellij.ide)
}

val props by localProperties()

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
    projectName = Config.name
// needed when plugin provides custom settings exposed to the UI
    buildSearchableOptions = false
    pluginVerification {
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
            sinceBuild = "241"
            untilBuild = "242.*"
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
    implementation(compose.desktop.common)
    implementation(projects.core)
    intellijPlatform {
        // bundledPlugin("org.jetbrains.kotlin")
        // props["plugin.local.ide.path"]?.toString()?.let(::local)
        // intellijIdeaCommunity("2024.2.1")
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
}

tasks {
    // workaround for https://youtrack.jetbrains.com/issue/IDEA-285839/Classpath-clash-when-using-coroutines-in-an-unbundled-IntelliJ-plugin
    buildPlugin {
        exclude { "coroutines" in it.name }
        archiveFileName = "valkyrie-$version.zip"
    }
    prepareSandbox {
        exclude { "coroutines" in it.name }
    }
}
