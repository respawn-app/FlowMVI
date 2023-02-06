@file:Suppress("MissingPackageDeclaration")

import Config.artifact
import Config.artifactId
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.build.gradle.tasks.BundleAar
import gradle.kotlin.dsl.accessors._7fbb8709bc469bf367d4d226f684fde5.implementation
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.internal.impldep.com.amazonaws.util.XpathUtils.asNode
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import java.net.URI
import java.util.Properties

/**
 * Configures Maven publishing to sonatype for this project
 */
@Suppress("unused")
fun Project.publishMultiplatform() {
    val properties = gradleLocalProperties(rootDir)
    val isReleaseBuild = properties["release"]?.toString().toBoolean()

    val javadocJar = tasks.register("javadocJar", Jar::class) {
        archiveClassifier.set("javadoc")
    }

    afterEvaluate {
        requireNotNull(extensions.findByType<PublishingExtension>()).apply {

            sonatypeRepository(isReleaseBuild, properties)

            publications.withType<MavenPublication>().configureEach {
                artifact(javadocJar)

                configurePom()
                configureVersion(isReleaseBuild)
            }
        }
        signPublications(properties)
    }
}

fun Project.publishAndroid() = afterEvaluate {
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
                    configurations.implementation.get().allDependencies.forEach {
                        if (it.name != "unspecified") {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", it.group)
                            dependencyNode.appendNode("artifactId", it.name)
                            dependencyNode.appendNode("version", it.version)
                        }
                    }
                }
            }
        }

        signPublications(properties)

        tasks.withType<AbstractPublishToMaven>().configureEach {
            dependsOn(tasks.withType<BundleAar>())
        }
    }
}

private fun MavenPublication.configureVersion(release: Boolean) {
    version = buildString {
        append(Config.versionName)
        if (!release) append("-SNAPSHOT")
    }
}

private fun MavenPublication.configurePom() = pom {
    name.set(Config.artifact)
    description.set("A simple, classic KMM MVI implementation based on coroutines")
    url.set("https://github.com/respawn-app/flowMVI")

    licenses {
        license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
        }
    }
    developers {
        developer {
            id.set("respawn-app")
            name.set("Respawn")
            email.set("hello@respawn.pro")
            url.set("https://respawn.pro")
            organization.set("Respawn")
            organizationUrl.set(url)
        }
    }
    scm {
        url.set("https://github.com/respawn-app/flowMVI.git")
    }
}

private fun PublishingExtension.sonatypeRepository(release: Boolean, properties: Properties) = repositories {
    maven {
        name = "sonatype"
        url = URI(
            if (release) {
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            } else {
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            }
        )
        credentials {
            username = properties["sonatypeUsername"]?.toString()
            password = properties["sonatypePassword"]?.toString()
        }
    }
}

private fun Project.signPublications(properties: Properties) =
    requireNotNull(extensions.findByType<SigningExtension>()).apply {
        val isReleaseBuild = properties["release"]?.toString().toBoolean()

        val publishing = requireNotNull(extensions.findByType<PublishingExtension>())

        val signingKey: String? = properties["signing.key"]?.toString()
        val signingPassword: String? = properties["signing.password"]?.toString()

        isRequired = isReleaseBuild

        if (signingKey != null && signingPassword != null) {
            println("Using in memory PGP keys for signing")
            useInMemoryPgpKeys(signingKey, signingPassword)
        } else {
            println("Using local.properties for signing")
        }

        sign(publishing.publications)

        tasks.withType<Sign>().configureEach {
            onlyIf { isReleaseBuild }
        }
        tasks.withType<AbstractPublishToMaven>().configureEach {
            dependsOn(tasks.withType<Sign>())
        }
    }
