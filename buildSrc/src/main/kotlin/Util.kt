@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "MissingPackageDeclaration",
    "UndocumentedPublicProperty",
    "UndocumentedPublicFunction"
)

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency
import java.util.Base64

/**
 * Load version catalog for usage in places where it is not available yet with gradle 7.x.
 * to obtain a version/lib use:
 * ```
 * val libs by versionCatalog
 * libs.findVersion("androidxCompose").get().toString()
 * libs.requireLib("androidx.core.ktx")
 * ```
 */
val Project.versionCatalog: Lazy<VersionCatalog>
    get() = lazy { extensions.getByType<VersionCatalogsExtension>().named("libs") }

fun VersionCatalog.requirePlugin(alias: String) = findPlugin(alias).get().toString()
fun VersionCatalog.requireLib(alias: String) = findLibrary(alias).get()
fun VersionCatalog.requireBundle(alias: String) = findBundle(alias).get()
fun VersionCatalog.requireVersion(alias: String) = findVersion(alias).get().toString()
val org.gradle.api.provider.Provider<PluginDependency>.id: String get() = get().pluginId

/**
 * Creates a java array initializer code for a list of strings.
 * Example: { "a", "b", "c" }
 */
fun List<String>.toJavaArrayString() = buildString {
    append("{")

    this@toJavaArrayString.forEachIndexed { i, it ->

        append("\"$it\"")

        if (i != this@toJavaArrayString.lastIndex) {
            append(", ")
        }
    }

    append("}")
}

fun String.toBase64() = Base64.getEncoder().encodeToString(toByteArray())
