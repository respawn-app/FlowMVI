import com.vanniktech.maven.publish.MavenPublishBaseExtension
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
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.dokka)
    alias(libs.plugins.atomicfu)
    // alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.maven.publish) apply false
    // plugins already on a classpath (conventions)
    // alias(libs.plugins.androidApplication) apply false
    // alias(libs.plugins.androidLibrary) apply false
    // alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    group = Config.artifactId
    version = Config.versionName
}

subprojects {
    plugins.withType<ComposeCompilerGradleSubplugin>().configureEach {
        the<ComposeCompilerGradlePluginExtension>().apply {
            featureFlags.addAll(ComposeFeatureFlag.OptimizeNonSkippingGroups)
            stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_definitions.txt")
            if (properties["enableComposeCompilerReports"] == "true") {
                val metricsDir = layout.buildDirectory.dir("compose_metrics")
                metricsDestination = metricsDir
                reportsDestination = metricsDir
            }
        }
    }
    afterEvaluate {
        extensions.findByType<MavenPublishBaseExtension>()?.run {
            val isReleaseBuild = properties["release"]?.toString().toBoolean()
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
    // TODO: Migrate to applying dokka plugin per-project in conventions
    if (name in setOf("sample", "debugger", "server")) return@subprojects
    apply(plugin = rootProject.libs.plugins.dokka.id)

    dependencies {
        dokkaPlugin(rootProject.libs.dokka.android)
    }
}

doctor {
    warnWhenJetifierEnabled = true
    warnWhenNotUsingParallelGC = true
    disallowMultipleDaemons = false
    javaHome {
        ensureJavaHomeMatches.set(false)
    }
}
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

atomicfu {
    dependenciesVersion = libs.versions.kotlinx.atomicfu.get()
    transformJvm = true
    jvmVariant = "VH"
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
