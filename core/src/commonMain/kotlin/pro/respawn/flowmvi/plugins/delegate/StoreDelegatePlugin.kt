package pro.respawn.flowmvi.plugins.delegate

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.childStorePlugin
import pro.respawn.flowmvi.plugins.compositePlugin

/**
 * Creates a new plugin that will:
 *
 * 1. If [start] is true, launch the provided [delegate] in the installed store's
 * [pro.respawn.flowmvi.api.PipelineContext]. See [childStorePlugin] for more details.
 * 2. Update the [delegate]'s [StoreDelegate.stateProjection] with the values from the delegated store according to the
 * [DelegationMode] provided to the [delegate], and handle actions using the provided [consume] function.
 *
 *
 * Warning: The delegate's [StoreDelegate.stateProjection] is not guaranteed to be up-to-date. It is a projection
 * based on [DelegationMode].
 *
 * @param delegate The store delegate to use
 * @param name Optional name for the plugin, by default allows only one plugin per delegate.
 * @param start Whether to automatically start the delegate store when the plugin is installed
 * @param blocking Whether to wait for the delegate store to start before continuing (applies if [start] is true)
 * @param consume Optional function to handle actions from the delegate store
 *
 * @return A store plugin that can be installed in the principal store
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    CS : MVIState,
    CI : MVIIntent,
    CA : MVIAction
    > storeDelegatePlugin(
    delegate: StoreDelegate<CS, CI, CA>,
    name: String? = delegate.name,
    start: Boolean = true,
    blocking: Boolean = false,
    consume: (suspend (CA) -> Unit)? = null,
): StorePlugin<S, I, A> = delegate.asPlugin<S, I, A>(name, consume).let {
    if (!start) return@let it
    compositePlugin(
        name = name,
        plugins = listOf(childStorePlugin(setOf(delegate.delegate), null, blocking), it),
    )
}

/**
 * Create a new [StoreDelegate] and install it as a [storeDelegatePlugin].
 *
 * The delegate can be used to project the state of the delegate store to the principal store and handle Actions using
 * [consume]. The [mode] will be used to determine how and when the state/actions are projected.
 *
 * ```kotlin
 * val store = store(Loading) {
 *     val feedState by delegate(feedStore) {
 *         // handle actions
 *     }
 *     whileSubscribed {
 *         feedState.collect { state ->
 *             // use projection.
 *         }
 *     }
 * }
 * ```
 *
 * @param store The store to delegate to
 * @param mode The delegation mode that determines when and how the delegate's state/actions are projected
 * @param name Optional name for the plugin, by default allows only one plugin per delegate.
 * @param start Whether to automatically start the delegate store when the plugin is installed
 * @param blocking Whether to wait for the delegate store to start before continuing (when [start] is true)
 * @param consume Optional function to handle actions from the delegate store
 * @see DelegationMode
 * @see childStorePlugin
 * @see StoreDelegate
 *
 * @return A [StoreDelegate] instance that can be used to access the delegate store's state
 */
@FlowMVIDSL
@OptIn(InternalFlowMVIAPI::class)
@ExperimentalFlowMVIAPI
public fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    CS : MVIState,
    CI : MVIIntent,
    CA : MVIAction
    > StoreBuilder<S, I, A>.delegate(
    store: Store<CS, CI, CA>,
    mode: DelegationMode = DelegationMode.Default,
    name: String? = "${store.name.orEmpty()}DelegatePlugin",
    start: Boolean = true,
    blocking: Boolean = false,
    consume: (suspend (CA) -> Unit)? = null,
): StoreDelegate<CS, CI, CA> = StoreDelegate(store, mode).also {
    install(storeDelegatePlugin(it, name, start, blocking, consume))
}
