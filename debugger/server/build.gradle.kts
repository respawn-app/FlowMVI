import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id(libs.plugins.kotlin.multiplatform.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

val parentNamespace = namespaceByPath()

// must be earlier than other config or build tasks
val generateBuildConfig by tasks.registering(Sync::class) {
    from(
        resources.text.fromString(
            """
                package $parentNamespace
                
                object BuildFlags {
                    const val VersionCode = ${Config.versionCode}
                    const val VersionName = "${Config.versionName}"
                    const val SupportEmail = "${Config.supportEmail}"
                    const val ProjectUrl = "${Config.url}"
                
                }
            """.trimIndent()
        )
    ) {
        rename { "BuildFlags.kt" }
        into(parentNamespace.replace(".", "/"))
    }
    // the target directory
    into(layout.buildDirectory.dir("generated/kotlin/src/commonMain"))
}

compose.resources {
    publicResClass = true
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = Config.jvmTarget.target
        targetCompatibility = Config.jvmTarget.target
    }
}
kotlin {
    jvm {
        compilerOptions {
            jvmTarget = Config.jvmTarget
        }
    }
    sourceSets {
        all {
            languageSettings {
                progressiveMode = true
                Config.optIns.forEach { optIn(it) }
            }
        }
        commonMain {
            kotlin.srcDir(generateBuildConfig.map { it.destinationDir })
        }
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.compose)
            implementation(projects.debugger.debuggerCommon)

            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)

            implementation(applibs.decompose.compose)
            implementation(applibs.decompose)
            implementation(projects.essenty.essentyCompose)
            implementation(projects.essenty)
            implementation(libs.bundles.serialization)
            implementation(libs.bundles.ktor.server)
            implementation(applibs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.collections)
            implementation(applibs.apiresult)
            implementation(applibs.bundles.koin)
            implementation(libs.kotlin.io)
            implementation(libs.kotlin.atomicfu)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
    }
}
