@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.atomicfu)
}

rootProject.group = rootProject.name
rootProject.version = "1.0.0-alpha01"

atomicfu {
    dependenciesVersion = libs.versions.kotlinx.atomicfu.get()
    transformJvm = true
    jvmVariant = "VH"
    transformJs = false
}

buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle.versions)
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
    }
}

versionCatalogUpdate {
    sortByKey.set(false)

    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}

allprojects {
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        // use rootProject as subprojects libs are ambiguous
        detektPlugins(rootProject.libs.detekt.formatting)
        detektPlugins(rootProject.libs.detekt.compose)
    }

    tasks {
        // needed to generate compose compiler reports. See /scripts
        withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            buildUponDefaultConfig = true
            parallel = true
            setSource(projectDir)
            config.setFrom(File(rootDir, Config.Detekt.configFile))
            basePath = projectDir.absolutePath

            jvmTarget = Config.jvmTarget
            include(Config.Detekt.includedFiles)
            exclude(Config.Detekt.excludedFiles)
            reports {
                xml.required.set(false)
                html.required.set(true)
                txt.required.set(false)
                sarif.required.set(true)
                md.required.set(false)
            }
        }
        withType<JavaCompile> {
            targetCompatibility = "11"
        }
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = freeCompilerArgs + Config.kotlinCompilerArgs
            }
        }
        withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>().configureEach {
            // outputFormatter = "json"

            fun stabilityLevel(version: String): Int {
                Config.stabilityLevels.forEachIndexed { index, postfix ->
                    val regex = ".*[.\\-]$postfix[.\\-\\d]*".toRegex(RegexOption.IGNORE_CASE)
                    if (version.matches(regex)) return index
                }
                return Config.stabilityLevels.size
            }

            rejectVersionIf {
                stabilityLevel(currentVersion) > stabilityLevel(candidate.version)
            }
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register<io.gitlab.arturbosch.detekt.Detekt>("detektFormat") {
        description = "Formats whole project."
        autoCorrect = true
    }

    register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
        description = "Run detekt on whole project"
        autoCorrect = false
    }
}
