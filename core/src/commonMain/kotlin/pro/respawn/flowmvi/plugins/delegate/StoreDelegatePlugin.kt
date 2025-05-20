package pro.respawn.flowmvi.plugins.delegate

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.childStorePlugin
import pro.respawn.flowmvi.plugins.compositePlugin

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> delegate(
    store: Store<S, I, A>,
    mode: DelegationMode = DelegationMode.Default,
): StoreDelegate<S, I, A> = StoreDelegate(delegate = store, mode = mode)

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

@FlowMVIDSL
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
