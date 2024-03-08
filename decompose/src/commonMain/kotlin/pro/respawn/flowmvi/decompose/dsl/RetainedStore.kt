@file:Suppress("Indentation") // conflict between detekt <> ide
package pro.respawn.flowmvi.decompose.dsl

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.decompose.api.RetainedStore
import pro.respawn.flowmvi.util.nameByType

// keeper

public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    key: Any = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = getOrCreate(key) { retained(factory(), scope) }

public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    factory: () -> Store<S, I, A>,
): Store<S, I, A> = getOrCreate(key) { retained(factory(), scope) }

// keeper owner

public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    factory: () -> Store<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(key, scope, factory)

public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    key: Any = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(key, scope, factory)
