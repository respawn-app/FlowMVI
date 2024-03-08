package pro.respawn.flowmvi.decompose.dsl

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.util.nameByType

// keeper

public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    initial: S,
    name: String,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = getOrCreate(name) {
    store(initial) {
        this.name = name
        builder()
    }.retain(scope)
}

public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    initial: S,
    scope: CoroutineScope? = retainedScope(),
    name: String = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = retainedStore(initial, name, scope, builder)

// owner

public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    initial: S,
    name: String,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(initial, name, scope, builder)

public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    initial: S,
    scope: CoroutineScope? = retainedScope(),
    name: String = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(initial, name, scope, builder)
