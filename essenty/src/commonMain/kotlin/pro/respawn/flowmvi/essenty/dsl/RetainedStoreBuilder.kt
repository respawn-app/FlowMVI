@file:MustUseReturnValues

package pro.respawn.flowmvi.essenty.dsl

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.essenty.api.DelicateRetainedApi
import pro.respawn.flowmvi.essenty.internal.retain
import kotlin.reflect.typeOf

// region keeper

/**
 * Creates and retains a new [Store] instance built using [builder] using this [InstanceKeeper].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 *
 * See [store] for more details.
 */
@FlowMVIDSL
@DelicateRetainedApi
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    initial: S,
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = getOrCreate(key) {
    store(initial, builder).retain(scope)
}

/**
 * Creates and retains a new [Store] instance built using [builder] using this [InstanceKeeper].
 *
 * * Uses the type of [S] as the key for the instance keeper
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 *
 * See [store] for more details.
 */
@FlowMVIDSL
@DelicateRetainedApi
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    initial: S,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = retainedStore(initial, typeOf<S>(), scope, builder)

// endregion

// region owner

/**
 * Creates and retains a new [Store] instance built using [builder] using this [InstanceKeeper].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 *
 * See [store] for more details.
 */
@FlowMVIDSL
@DelicateRetainedApi
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    initial: S,
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(initial, key, scope, builder)

/**
 * Creates and retains a new [Store] instance built using [builder] using this [InstanceKeeper].
 *
 * * Uses the type of [S] as the key for the instance keeper
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 *
 * See [store] for more details.
 */
@FlowMVIDSL
@DelicateRetainedApi
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    initial: S,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference builder: BuildStore<S, I, A>,
): Store<S, I, A> = retainedStore(initial, typeOf<S>(), scope, builder)
