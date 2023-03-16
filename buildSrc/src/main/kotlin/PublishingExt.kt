@file:Suppress("MissingPackageDeclaration")

import gradle.kotlin.dsl.accessors._7fbb8709bc469bf367d4d226f684fde5.api
import gradle.kotlin.dsl.accessors._7fbb8709bc469bf367d4d226f684fde5.compileOnly
import gradle.kotlin.dsl.accessors._7fbb8709bc469bf367d4d226f684fde5.implementation
import gradle.kotlin.dsl.accessors._7fbb8709bc469bf367d4d226f684fde5.runtimeOnly
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import java.net.URI
import java.util.Properties

fun MavenPublication.configureVersion(release: Boolean) {
    version = buildString {
        append(Config.versionName)
        if (!release) append("-SNAPSHOT")
    }
}

fun MavenPublication.configurePom() = pom {
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

fun PublishingExtension.sonatypeRepository(release: Boolean, properties: Properties) = repositories {
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

fun Project.signPublications(properties: Properties) =
    requireNotNull(extensions.findByType<SigningExtension>()).apply {
        val isReleaseBuild = properties["release"]?.toString().toBoolean()

        val publishing = requireNotNull(extensions.findByType<PublishingExtension>())

        val signingKey: String? = properties["signing.key"]?.toString()
        val signingPassword: String? = properties["signing.password"]?.toString()

        isRequired = isReleaseBuild

        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }

        sign(publishing.publications)

        tasks.run {
            withType<Sign>().configureEach {
                onlyIf { isReleaseBuild }
            }

            withType<AbstractPublishToMaven>().configureEach {
                dependsOn(tasks.withType<Sign>())
            }
        }
    }

val ConfigurationContainer.mavenScoped
    get() = mapOf(
        runtimeOnly.get() to "runtime",
        api.get() to "compile",
        implementation.get() to "compile",
        compileOnly.get() to "provided"
    )
