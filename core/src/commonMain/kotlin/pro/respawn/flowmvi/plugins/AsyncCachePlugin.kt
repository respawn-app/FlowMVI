package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.StoreBuilder
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Alias for [Deferred.await]
 */
public suspend operator fun <T> Deferred<T>.invoke(): T = await()

/**
 * Create a new [CachedValue] but run the [init] in an asynchronous way and return a [Deferred] that can be used
 * to await the value
 * @return A [CachedValue] granting access to the value returned from [init]
 * @see cached
 * @see cachePlugin
 * @see Deferred
 */
@FlowMVIDSL
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> asyncCached(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    crossinline init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<Deferred<T>, S, I, A> = cached { async(context, start) { init() } }

/**
 * Install a new [cachePlugin] that will run the [init] in an asynchronous way and return a [Deferred] that can be used
 * to await the value.
 *
 * @return A [CachedValue] granting access to the value returned from [init]
 * @see cached
 * @see cachePlugin
 * @see asyncCached
 * @see Deferred
 */
@FlowMVIDSL
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.asyncCache(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    crossinline init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<Deferred<T>, S, I, A> = asyncCached(context, start, init).also { install(cachePlugin(it)) }
