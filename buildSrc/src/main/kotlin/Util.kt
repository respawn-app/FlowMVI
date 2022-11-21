import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

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
    get() = lazy {
        extensions.getByType<VersionCatalogsExtension>().named("libs")
    }

fun CommonExtension<*, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}

fun VersionCatalog.requirePlugin(alias: String) = findPlugin(alias).get().toString()
fun VersionCatalog.requireLib(alias: String) = findLibrary(alias).get()
fun VersionCatalog.requireBundle(alias: String) = findBundle(alias).get()

val org.gradle.api.provider.Provider<PluginDependency>.id: String get() = get().pluginId
