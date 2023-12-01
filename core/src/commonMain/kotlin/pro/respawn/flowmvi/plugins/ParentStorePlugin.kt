package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.subscribe

// TODO: Because of a compiler bug, won't let assign a default value to the "render" param

/**
 * An overload of the [parentStorePlugin] that also consumes its [MVIAction]s.
 * Please see the other overload for more documentation.
 *
 * @see parentStorePlugin
 * @see parentStore
 */
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    S2 : MVIState,
    I2 : MVIIntent,
    A2 : MVIAction
    > parentStorePlugin(
    parent: Store<S2, I2, A2>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline consume: suspend PipelineContext<S, I, A>.(action: A2) -> Unit,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): StorePlugin<S, I, A> = whileSubscribedPlugin(name = name, minSubscriptions = minExternalSubscriptions) {
    // do not use pipeline context to cancel subscription properly, suspend instead
    coroutineScope {
        subscribe(parent, { consume(it) }, { render(it) }).join()
    }
}

/**
 * Creates and installs a new plugin that will subscribe to the [parent] store at the same time the current store is
 * subscribed to and when [minExternalSubscriptions] are reached.
 *
 * This plugin will subscribe to the parent store and [render] its states while there are [minExternalSubscriptions] of
 * the current store present.
 * When the subscribers leave as in [whileSubscribedPlugin], this store will also unsubscribe from the parent store.
 *
 * Essentially, this store will be a subscriber of another store while this store is also subscribed to externally.
 * For the behavior where the store will always be subscribed, please subscribe in the [initPlugin].
 *
 * This function will not consume [MVIAction]s of the parent store. For that, please see the other overload of this
 * function.
 *
 * The name of this plugin will be derived from the parent store's name, if present, otherwise `null`.
 *
 * @see parentStorePlugin
 * @see parentStore
 */
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction, S2 : MVIState, I2 : MVIIntent> parentStorePlugin(
    parent: Store<S2, I2, *>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): StorePlugin<S, I, A> = whileSubscribedPlugin(name = name, minSubscriptions = minExternalSubscriptions) {
    coroutineScope {
        subscribe(parent) { render(it) }.join()
    }
}

/**
 * Install a new [parentStorePlugin]. This overload **does not** collect the parent store's actions.
 * @see parentStorePlugin
 */
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    S2 : MVIState,
    I2 : MVIIntent,
    > StoreBuilder<S, I, A>.parentStore(
    parent: Store<S2, I2, *>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): Unit = install(parentStorePlugin(parent, name, minExternalSubscriptions, render = render))

/**
 * Install a new [parentStorePlugin].
 * @see parentStorePlugin
 */
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    S2 : MVIState,
    I2 : MVIIntent,
    A2 : MVIAction,
    > StoreBuilder<S, I, A>.parentStore(
    parent: Store<S2, I2, A2>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline consume: suspend PipelineContext<S, I, A>.(action: A2) -> Unit,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): Unit = install(parentStorePlugin(parent, name, minExternalSubscriptions, consume = consume, render = render))
