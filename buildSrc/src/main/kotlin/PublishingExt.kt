@file:Suppress("MissingPackageDeclaration")

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import java.net.URI
import java.util.Properties

internal fun MavenPublication.configureVersion(release: Boolean) {
    version = buildString {
        append(Config.versionName)
        if (!release) append("-SNAPSHOT")
    }
}

internal fun MavenPublication.configurePom() = pom {
    name.set(Config.artifact)
    description.set(Config.description)
    url.set(Config.url)

    licenses {
        license {
            name.set(Config.licenseName)
            url.set(Config.licenseUrl)
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
        url.set(Config.scmUrl)
    }
}

internal fun PublishingExtension.sonatypeRepository(release: Boolean, localProps: Properties) = repositories {
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
            username = localProps["sonatypeUsername"]?.toString()
            password = localProps["sonatypePassword"]?.toString()
        }
    }
}

internal fun Project.signPublications(isRelease: Boolean, localProps: Properties) =
    requireNotNull(extensions.findByType<SigningExtension>()).apply {
        val publishing = requireNotNull(extensions.findByType<PublishingExtension>())

        val signingKey: String? = localProps["signing.key"]?.toString()
        val signingPassword: String? = localProps["signing.password"]?.toString()

        isRequired = isRelease

        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }

        sign(publishing.publications)

        tasks.run {
            withType<Sign>().configureEach {
                onlyIf { isRelease }
            }

            withType<AbstractPublishToMaven>().configureEach {
                dependsOn(tasks.withType<Sign>())
            }
        }
    }
