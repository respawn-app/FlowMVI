import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import nl.littlerobots.vcu.plugin.versionCatalogUpdate
import nl.littlerobots.vcu.plugin.versionSelector
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.detekt)
    // alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.version.catalog.update)
    // alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.atomicfu) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.benchmark) apply false
    // plugins already on a classpath (conventions)
    // alias(libs.plugins.androidApplication) apply false
    // alias(libs.plugins.androidLibrary) apply false
    // alias(libs.plugins.kotlin.multiplatform) apply false
    // alias(libs.plugins.kotlinMultiplatform) apply false
    dokkaDocumentation
}

allprojects {
    group = Config.artifactId
    version = Config.versionName
}

subprojects {
    plugins.withType<ComposeCompilerGradleSubplugin>().configureEach {
        the<ComposeCompilerGradlePluginExtension>().apply {
            featureFlags.addAll(ComposeFeatureFlag.OptimizeNonSkippingGroups)
            stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_definitions.txt"))
            if (properties["enableComposeCompilerReports"] == "true") {
                val metricsDir = layout.buildDirectory.dir("compose_metrics")
                metricsDestination = metricsDir
                reportsDestination = metricsDir
            }
        }
    }
    plugins.withType<MavenPublishBasePlugin> {
        the<MavenPublishBaseExtension>().apply {
            val isReleaseBuild = properties["release"]?.toString().toBoolean()
            configure(
                KotlinMultiplatform(
                    javadocJar = JavadocJar.Empty(),
                    sourcesJar = true,
                    androidVariantsToPublish = listOf("release"),
                )
            )
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, false)
            if (isReleaseBuild) signAllPublications()
            coordinates(Config.artifactId, name, Config.version(isReleaseBuild))
            pom {
                name = Config.name
                description = Config.description
                url = Config.url
                licenses {
                    license {
                        name = Config.licenseName
                        url = Config.licenseUrl
                        distribution = Config.licenseUrl
                    }
                }
                developers {
                    developer {
                        id = Config.vendorId
                        name = Config.vendorName
                        url = Config.developerUrl
                        email = Config.supportEmail
                        organizationUrl = Config.developerUrl
                    }
                }
                scm {
                    url = Config.scmUrl
                }
            }
        }
    }
    tasks {
        withType<Test>().configureEach {
            useJUnitPlatform()
            filter { isFailOnNoMatchingTests = true }
        }
    }
}

// TODO: Incompatible with gradle isolated projects
// doctor {
//     warnWhenJetifierEnabled = true
//     warnWhenNotUsingParallelGC = true
//     disallowMultipleDaemons = false
//     javaHome {
//         ensureJavaHomeMatches.set(false)
//     }
// }
//
// dependencyAnalysis {
//     structure {
//         ignoreKtx(true)
//     }
// }

dependencies {
    detektPlugins(rootProject.libs.detekt.formatting)
    detektPlugins(rootProject.libs.detekt.compose)
    detektPlugins(rootProject.libs.detekt.libraries)
    projects.run {
        listOf(
            core,
            essenty,
            savedstate,
            android,
            compose,
            essenty.essentyCompose,
            test,
            debugger.debuggerClient,
            debugger.debuggerPlugin,
        ).forEach { dokka(it) }
    }
}

versionCatalogUpdate {
    sortByKey = true

    versionSelector { stabilityLevel(it.candidate.version) >= Config.minStabilityLevel }

    keep {
        keepUnusedVersions = true
        keepUnusedLibraries = true
        keepUnusedPlugins = true
    }
}

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
}

rootProject.plugins.withType<YarnPlugin>().configureEach {
    rootProject.the<YarnRootExtension>().apply {
        yarnLockMismatchReport = YarnLockMismatchReport.WARNING // NONE | FAIL | FAIL_AFTER_BUILD
        reportNewYarnLock = true
        yarnLockAutoReplace = true
    }
}
