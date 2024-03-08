package pro.respawn.flowmvi.decompose.dsl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.decompose.api.RetainedStore
import pro.respawn.flowmvi.util.nameByType

public fun <S : MVIState, I : MVIIntent, A : MVIAction> retained(
    store: Store<S, I, A>,
): RetainedStore<S, I, A> = object : Store<S, I, A> by store, RetainedStore<S, I, A> {}

public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    store: Store<S, I, A>,
    key: Any = store.name ?: nameByType<S>()?.let { "${it}Store" } ?: store::class,
): Store<S, I, A> = getOrCreate(key = key) { retained(store) }

public fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    key: Any,
    store: Store<S, I, A>,
): Store<S, I, A> = getOrCreate(key = key) { retained(store) }

public fun <S : MVIState, I : MVIIntent, A : MVIAction> ComponentContext.retainedStore(
    key: Any,
    store: Store<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(key, store)

public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> ComponentContext.retainedStore(
    store: Store<S, I, A>,
    key: Any = store.name ?: nameByType<S>()?.let { "${it}Store" } ?: store::class,
): Store<S, I, A> = instanceKeeper.retainedStore(store, key)
