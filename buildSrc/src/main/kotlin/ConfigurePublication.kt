@file:Suppress("MissingPackageDeclaration")

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.build.gradle.tasks.BundleAar
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign

/**
 * Configures Maven publishing to sonatype for this project
 */
@Suppress("unused")
fun Project.publishMultiplatform() {
    val properties = gradleLocalProperties(rootDir)
    val isReleaseBuild = properties["release"]?.toString().toBoolean()
    val dokkaJavadocJar = tasks.named("dokkaJavadocJar")

    afterEvaluate {
        requireNotNull(extensions.findByType<PublishingExtension>()).apply {
            sonatypeRepository(isReleaseBuild, properties)

            publications.withType<MavenPublication>().configureEach {
                artifact(dokkaJavadocJar)
                configurePom()
                configureVersion(isReleaseBuild)
            }
        }
        signPublications(properties)
    }
}

/**
 * Publish the android artifact
 */
fun Project.publishAndroid() {
    afterEvaluate {
        val properties = gradleLocalProperties(rootDir)
        val isReleaseBuild = properties["release"]?.toString().toBoolean()

        requireNotNull(extensions.findByType<PublishingExtension>()).apply {
            sonatypeRepository(isReleaseBuild, properties)

            publications {
                create("release", MavenPublication::class.java) {
                    artifact("$buildDir/outputs/aar/${project.name}-release.aar")
                    groupId = rootProject.group.toString()
                    artifactId = project.name

                    configurePom()
                    configureVersion(isReleaseBuild)

                    pom.withXml {
                        val dependenciesNode = asNode().appendNode("dependencies")
                        configurations.mavenScoped.forEach { it, scope ->
                            it.allDependencies.all {
                                if (group == null || version == null || name == "unspecified") return@all

                                val node = dependenciesNode.appendNode("dependency")
                                node.appendNode("groupId", group)
                                node.appendNode("artifactId", name)
                                node.appendNode("version", version)
                                node.appendNode("scope", scope)
                            }
                        }
                    }
                }
            }
        }
        signPublications(properties)
    }

    tasks.withType<Sign>().configureEach {
        dependsOn(tasks.withType<BundleAar>())
    }
}
