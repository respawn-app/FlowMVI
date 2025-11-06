@file:Suppress("Indentation") // conflict between detekt <> ide
@file:MustUseReturnValue
package pro.respawn.flowmvi.essenty.dsl

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.internal.retained
import kotlin.jvm.JvmName
import kotlin.reflect.typeOf

// region keeper

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
@JvmName("retainedInstanceKeeperStoreFactory")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = getOrCreate(key) { retained(factory(), scope) }

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * Uses the type of [S] as the key for the instance keeper
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeper.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = retainedStore(typeOf<S>(), scope, factory)

// endregion

// region keeper owner

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * Uses the type of [S] as the key for the instance keeper
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
@JvmName("retainedInstanceKeeperOwnerStoreFactory")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference factory: () -> Store<S, I, A>,
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
    @BuilderInference factory: () -> Store<S, I, A>,
): Store<S, I, A> = retainedStore(typeOf<S>(), scope, factory)

// endregion

// region container

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeperOwner].
 *
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
@JvmName("retainedContainerFactory")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    key: Any,
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference factory: () -> Container<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(
    key = key, scope = scope
) { factory().store }

/**
 * Creates and retains a new [Store] instance provided using [factory] using this [InstanceKeeper].
 *
 * * Uses the type of [S] as the key for the instance keeper
 * * By default, uses a [retainedScope] instance to launch the store automatically.
 *   Provide `null` to not launch the store after creation.
 */
@FlowMVIDSL
@JvmName("subscribeContainerFactory")
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> InstanceKeeperOwner.retainedStore(
    scope: CoroutineScope? = retainedScope(),
    @BuilderInference factory: () -> Container<S, I, A>,
): Store<S, I, A> = instanceKeeper.retainedStore(scope) { factory().store }

// endregion
