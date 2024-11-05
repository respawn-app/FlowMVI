@file:OptIn(DelicateStoreApi::class)

package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle

/**
 * Coroutine context consists of the following: job, name, handler, dispatcher.
 * here we create a new child coroutine context:
 *
 * * using the dispatcher & other elements of the scope
 * * using a supervisor job that is a child of the parent scope's job
 * * using coroutine name of the store
 * * using exception handler that uses the scope itself to recover from unhandled exceptions
 * * using this pipeline instance as the context element
 */
@OptIn(DelicateStoreApi::class)
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.launchPipeline(
    parent: CoroutineScope,
    config: StoreConfiguration<S>,
    crossinline onStop: (e: Exception?) -> Unit,
    crossinline onAction: suspend PipelineContext<S, I, A>.(action: A) -> Unit,
    crossinline onTransformState: suspend PipelineContext<S, I, A>.(transform: suspend S.() -> S) -> Unit,
    onStart: PipelineContext<S, I, A>.(lifecycle: StoreLifecycleModule) -> Unit,
): StoreLifecycle where T : IntentReceiver<I>, T : StateReceiver<S>, T : RecoverModule<S, I, A> {
    val job = SupervisorJob(parent.coroutineContext[Job]).apply {
        invokeOnCompletion {
            when (it) {
                null, is CancellationException -> onStop(null)
                !is Exception -> throw it
                else -> onStop(it)
            }
        }
    }
    return object :
        IntentReceiver<I> by this,
        StateReceiver<S> by this,
        PipelineContext<S, I, A>,
        StoreLifecycleModule by storeLifecycle(job),
        ActionReceiver<A> {

        override val config get() = config
        override val key = PipelineContext // recoverable should be separate from this key
        private val handler = PipelineExceptionHandler()
        private val pipelineName = CoroutineName(toString())

        override val coroutineContext = parent.coroutineContext +
            config.coroutineContext +
            pipelineName +
            job +
            handler +
            this

        override fun toString(): String = "${config.name.orEmpty()}PipelineContext"

        override suspend fun updateState(transform: suspend S.() -> S) = catch { onTransformState(transform) }
        override suspend fun action(action: A) = catch { onAction(action) }
        override fun send(action: A) {
            launch { action(action) }
        }
    }.apply {
        onStart(this)
    }
}
