@file:Suppress("MissingPackageDeclaration", "unused")

import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.tasks.BundleAar
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign

/**
 * Configures Maven publishing to sonatype for this project
 */
fun Project.publishMultiplatform() {
    val properties = localProperties()
    val isReleaseBuild = properties["release"]?.toString().toBoolean()
    val javadocTask = tasks.named("emptyJavadocJar") // TODO: dokka does not support kmp javadocs yet

    afterEvaluate {
        requireNotNull(extensions.findByType<PublishingExtension>()).apply {
            publications.withType<MavenPublication>().configureEach {
                groupId = rootProject.group.toString()
                artifact(javadocTask)
                configurePom()
                configureVersion(isReleaseBuild)
            }
            sonatypeRepository(isReleaseBuild, properties)
        }
        signPublications(isReleaseBuild, properties)
    }

    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(javadocTask)
    }
}

/**
 * Publish the android artifact
 */
fun Project.publishAndroid(ext: LibraryExtension) = with(ext) {
    val properties = localProperties()
    val isReleaseBuild = requireNotNull(properties["release"]).toString().toBooleanStrict()
    publishing {
        singleVariant(Config.publishingVariant) {
            withSourcesJar()
            withJavadocJar()
        }
    }

    afterEvaluate {
        requireNotNull(extensions.findByType<PublishingExtension>()).apply {
            publications.maybeCreate(Config.publishingVariant, MavenPublication::class).apply {
                from(components[Config.publishingVariant])
                groupId = rootProject.group.toString()
                configurePom()
                configureVersion(isReleaseBuild)
            }
            sonatypeRepository(isReleaseBuild, properties)
        }
        signPublications(isReleaseBuild, properties)
    }

    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(tasks.withType<BundleAar>())
    }
    tasks.withType<Sign>().configureEach {
        dependsOn(tasks.withType<BundleAar>())
    }
}
