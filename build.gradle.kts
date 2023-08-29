import nl.littlerobots.vcu.plugin.versionCatalogUpdate
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.dokka)
    // TODO: https://github.com/Kotlin/kotlinx-atomicfu/issues/281
    // alias(libs.plugins.atomicfu)
    alias(libs.plugins.dependencyAnalysis)
}

buildscript {
    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.version.gradle)
        classpath(libs.detekt.gradle)
    }
}

allprojects {
    group = Config.artifactId
    version = Config.versionName
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(Config.jvmTarget)
            languageVersion.set(Config.kotlinVersion)
            freeCompilerArgs.addAll(Config.compilerArgs)
            optIn.addAll(Config.optIns.map { "-opt-in=$it" })
        }
    }
}

subprojects {
    if (name == "app") return@subprojects
    apply(plugin = rootProject.libs.plugins.dokka.id)

    dependencies {
        dokkaPlugin(rootProject.libs.dokka.android)
    }

    tasks {
        withType<Test>().configureEach {
            useJUnitPlatform()
            filter { isFailOnNoMatchingTests = true }
        }
        register<org.gradle.jvm.tasks.Jar>("dokkaJavadocJar") {
            dependsOn(dokkaJavadoc)
            from(dokkaJavadoc.flatMap { it.outputDirectory })
            archiveClassifier.set("javadoc")
        }

        register<org.gradle.jvm.tasks.Jar>("emptyJavadocJar") {
            archiveClassifier.set("javadoc")
        }
    }
}

doctor {
    javaHome {
        ensureJavaHomeMatches.set(false)
    }
}

dependencyAnalysis {
    issues {
        all {
            ignoreKtx(true)
        }
    }
}

dependencies {
    detektPlugins(rootProject.libs.detekt.formatting)
    detektPlugins(rootProject.libs.detekt.compose)
    detektPlugins(rootProject.libs.detekt.libraries)
}

versionCatalogUpdate {
    sortByKey.set(true)

    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}

// atomicfu {
//     dependenciesVersion = libs.versions.kotlinx.atomicfu.get()
//     transformJvm = false
//     jvmVariant = "VH"
//     transformJs = false
// }

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        buildUponDefaultConfig = true
        parallel = true
        setSource(projectDir)
        config.setFrom(File(rootDir, Config.Detekt.configFile))
        basePath = projectDir.absolutePath
        jvmTarget = Config.jvmTarget.target
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

    register<io.gitlab.arturbosch.detekt.Detekt>("detektFormat") {
        description = "Formats whole project."
        autoCorrect = true
    }

    register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
        description = "Run detekt on whole project"
        autoCorrect = false
    }

    withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>().configureEach {
        outputFormatter = "json"

        fun stabilityLevel(version: String): Int {
            Config.stabilityLevels.forEachIndexed { index, postfix ->
                val regex = """.*[.\-]$postfix[.\-\d]*""".toRegex(RegexOption.IGNORE_CASE)
                if (version.matches(regex)) return index
            }
            return Config.stabilityLevels.size
        }

        rejectVersionIf {
            stabilityLevel(currentVersion) > stabilityLevel(candidate.version)
        }
    }
}

extensions.findByType<YarnRootExtension>()?.run {
    yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    reportNewYarnLock = true
    yarnLockAutoReplace = false
}
