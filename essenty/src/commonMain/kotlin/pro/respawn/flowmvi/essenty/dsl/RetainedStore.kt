@file:Suppress("Indentation") // conflict between detekt <> ide
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
import pro.respawn.flowmvi.essenty.internal.retained
import pro.respawn.flowmvi.util.nameByType

// keeper

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * Uses the State class name as a key for the instance keeper by default.
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    key: Any = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = getOrCreate(key) { retained(factory(), scope) }

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    factory: () -> Store<S, I, A>,
): Store<S, I, A> = getOrCreate(key) { retained(factory(), scope) }

// keeper owner

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * Uses the State class name as a key for the instance keeper by default.
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    factory: () -> Store<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(key, scope, factory)

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    key: Any = "${requireNotNull(nameByType<S>())}Store",
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(key, scope, factory)
