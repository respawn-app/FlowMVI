package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.StoreBuilder
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
public class CachePlugin<out T, S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    name: String? = null,
    private val init: suspend PipelineContext<S, I, A>.() -> T,
) : AbstractStorePlugin<S, I, A>(name), ReadOnlyProperty<Any?, T> {

    private data object UNINITIALIZED {

        override fun toString() = "Uncached value"
    }

    private var _value = atomic<Any?>(UNINITIALIZED)

    /**
     * Returns true if the cached value is present
     */
    public val isCached: Boolean get() = _value.value !== UNINITIALIZED

    override suspend fun PipelineContext<S, I, A>.onStart(): Unit = _value.update { init() }

    override fun onStop(e: Exception?): Unit = _value.update { UNINITIALIZED }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        require(isCached) { AccessBeforeCachingMessage }
        return _value.value as T
    }
}

/**
 * Creates and returns a new [CachePlugin] without installing it.
 * @see CachePlugin
 */
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> cachePlugin(
    name: String? = null,
    @BuilderInference init: suspend PipelineContext<S, I, A>.() -> T,
): CachePlugin<T, S, I, A> = CachePlugin(name, init)

/**
 * Creates and installs a new [CachePlugin], returning a delegate that can be used to get access to the property that
 * was cached. Please consult the documentation of the parent class to understand how to use this plugin.
 *
 * @return A [ReadOnlyProperty] granting access to the value returned from [init]
 * @see cachePlugin
 */
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.cache(
    name: String? = null,
    @BuilderInference init: suspend PipelineContext<S, I, A>.() -> T,
): ReadOnlyProperty<Any?, T> = cachePlugin(name, init).also { install(it) }
