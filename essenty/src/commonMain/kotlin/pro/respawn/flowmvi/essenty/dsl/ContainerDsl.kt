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
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.store
import kotlin.reflect.typeOf

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
    key: Any = typeOf<S>(),
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> where T : Container<S, I, A>, T : InstanceKeeperOwner = instanceKeeper.retainedStore(
    key, scope, factory
)

/**
 * Creates and retains a new [Store] instance built using [builder] using this [InstanceKeeper].
 *
 * * Uses [name] as both the store name and the instance keeper's key parameter.
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 *
 * See [store] for more details.
 */
@FlowMVIDSL
public inline fun <T, reified S : MVIState, I : MVIIntent, A : MVIAction> T.retainedStore(
    initial: S,
    name: String,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> where T : Container<S, I, A>, T : InstanceKeeperOwner = instanceKeeper.retainedStore(
    initial, name, scope, builder
)

/**
 * Creates and retains a new [Store] instance built using [builder] using this [InstanceKeeper].
 *
 * * Uses [name] as both the store name and the instance keeper's key parameter. By default, the store's name will be
 *   derived from the [S] parameter's class name, such as 'CounterState' -> 'CounterStore'.
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 *
 * See [store] for more details.
 */
@FlowMVIDSL
public inline fun <T, reified S : MVIState, I : MVIIntent, A : MVIAction> T.retainedStore(
    initial: S,
    scope: CoroutineScope? = retainedScope(),
    key: Any = typeOf<S>(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> where T : Container<S, I, A>, T : InstanceKeeperOwner = instanceKeeper.retainedStore(
    initial, key, scope, builder
)
