package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private const val AccessBeforeCachingMessage = """
Cached value was accessed before the store was started. 
Please make sure you access the property only after onStart() and before onStop() have been called
and the plugin that caches the value was installed before first access.
"""

/**
 * A plugin that allows to cache the value of a property, scoping it to the [pro.respawn.flowmvi.api.Store]'s lifecycle.
 * This plugin will clear the value when [PipelineContext] is canceled and call the [init] block each time a store is
 * started to initialize it again.
 *
 * This plugin is similar to [lazy] but where value is also bound to the [pro.respawn.flowmvi.api.Store]'s lifecycle.
 * This plugin can be used to get access to the [PipelineContext] and execute suspending operations in to
 * initialize the value.
 *
 * The [init] block can be called **multiple times** in rare cases of concurrent access.
 * In practice, this should not happen
 *
 * This plugin is useful with legacy APIs that rely on the [kotlinx.coroutines.CoroutineScope]
 * to be present during the lifetime of a value, such as paging, and can be used to obtain the value in plugins such
 * as [whileSubscribedPlugin] without recreating it.
 *
 * The cached value **must not be accessed** before [pro.respawn.flowmvi.api.StorePlugin.onStart] and after
 * [pro.respawn.flowmvi.api.StorePlugin.onStop] have been called, where it will be uninitialized.
 * The [init] block is evaluated in [pro.respawn.flowmvi.api.StorePlugin.onStart] in the order the
 * cache plugin was installed.
 *
 * This plugin should **not be used** to run operations in [PipelineContext]. Use [initPlugin] for that.
 *
 * Access the delegated property as follows:
 *
 * ```kotlin
 * // in store's scope
 * val pagedItems: Flow<PagingData<Item>> by cache { // this: PipelineContext ->
 *     repo.pagedItems().cachedIn(this)
 * }
 * ```
 *
 * @see cache
 * @see cachePlugin
 */
public class CachedValue<out T, S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    private val init: suspend PipelineContext<S, I, A>.() -> T,
) : ReadOnlyProperty<Any?, T> {

    private data object UNINITIALIZED {

        override fun toString() = "Uninitialized cache value"
    }

    private var _value = atomic<Any?>(UNINITIALIZED)

    /**
     * Returns true if the cached value is present
     */
    public val isCached: Boolean get() = _value.value !== UNINITIALIZED

    /**
     * Obtain the value.
     * **The value can only be accessed before [StorePlugin.onStart] and [StorePlugin.onStop], otherwise this function
     * will throw!**
     */
    public val value: T
        @Suppress("UNCHECKED_CAST") get() {
            require(isCached) { AccessBeforeCachingMessage }
            return _value.value as T
        }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    internal fun asPlugin(name: String?) = plugin {
        this.name = name

        onStart { _value.update { init() } }

        onStop { _value.update { UNINITIALIZED } }
    }
}

/**
 * Creates and returns a new [CachedValue].
 * @see cachePlugin
 */
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> cached(
    @BuilderInference init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<T, S, I, A> = CachedValue(init)

@FlowMVIDSL
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> cachePlugin(
    value: CachedValue<T, S, I, A>,
    name: String? = null,
): StorePlugin<S, I, A> = value.asPlugin(name)

/**
 * Creates and installs a new [CachedValue], returning a delegate that can be used to get access to the property that
 * was cached. Please consult the documentation of the parent class to understand how to use this plugin.
 *
 * @return A [CachedValue] granting access to the value returned from [init]
 * @see cachePlugin
 * @see cached
 */
@FlowMVIDSL
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.cache(
    name: String? = null,
    @BuilderInference init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<T, S, I, A> = CachedValue(init).also { install(it.asPlugin(name)) }
