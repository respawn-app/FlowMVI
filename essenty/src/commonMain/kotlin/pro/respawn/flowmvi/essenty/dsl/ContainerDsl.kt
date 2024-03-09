package pro.respawn.flowmvi.essenty.dsl

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.util.nameByType

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeperOwner].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> T.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    factory: () -> Store<S, I, A>,
): Store<S, I, A> where T : Container<S, I, A>, T : InstanceKeeperOwner = instanceKeeper.retainedStore(
    key, scope, factory
)

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <T, reified S : MVIState, I : MVIIntent, A : MVIAction> T.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    key: Any = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> where T : Container<S, I, A>, T : InstanceKeeperOwner = instanceKeeper.retainedStore(
    key, scope, factory
)
