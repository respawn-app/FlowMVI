package pro.respawn.flowmvi.modules

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.StorePlugin

internal interface PluginEventHook<S : MVIState, I : MVIIntent, A : MVIAction> :
    Recoverable<S, I, A>,
    PipelineSubscriber<S, I, A> {

    suspend fun PipelineContext<S, I, A>.onIntent(intent: I)
    suspend fun PipelineContext<S, I, A>.onException(e: Exception)
    fun PipelineContext<S, I, A>.onSubscribe(subscriberScope: CoroutineScope)
    fun PipelineContext<S, I, A>.onUnsubscribe()
}

internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.pluginModule(
    plugins: Set<StorePlugin<S, I, A>>
): PluginEventHook<S, I, A> where T : StateReceiver<S>, T : ActionReceiver<A>, T : IntentReceiver<I> =
    object : PluginEventHook<S, I, A>, StateReceiver<S> by this, ActionReceiver<A> by this, IntentReceiver<I> by this {

        private var subscriberCount by atomic(0)

        override suspend fun PipelineContext<S, I, A>.recover(e: Exception) {
            plugins(e) { onException(e) }?.let { throw it }
        }

        override fun PipelineContext<S, I, A>.onStart() {
            launch {
                plugins { onStart() }
            }
        }

        override suspend fun PipelineContext<S, I, A>.onIntent(
            intent: I
        ) {
            plugins(intent) { onIntent(it) } // TODO: Throw on unhandled intents?
        }

        override suspend fun PipelineContext<S, I, A>.onAction(action: A) {
            plugins(action) { onAction(it) }?.let { this@pluginModule.send(it) }
        }

        override suspend fun PipelineContext<S, I, A>.onException(e: Exception) {
        }

        override fun PipelineContext<S, I, A>.onUnsubscribe() {
            --subscriberCount
            plugins { onUnsubscribe(subscriberCount) }
        }

        override fun onStop(e: Exception?) = plugins { onStop(e) }
        override suspend fun PipelineContext<S, I, A>.onTransformState(transform: suspend S.() -> S) {
            this@pluginModule.updateState {
                plugins(transform()) { onState(this@updateState, it) } ?: this
            }
        }

        override fun PipelineContext<S, I, A>.onSubscribe(subscriberScope: CoroutineScope) {
            plugins { onSubscribe(subscriberScope, subscriberCount) }
            ++subscriberCount
        }

        private inline fun plugins(block: StorePlugin<S, I, A>.() -> Unit) = plugins.forEach(block)
        private inline fun <R> plugins(
            initial: R,
            block: StorePlugin<S, I, A>.(R) -> R?
        ) = plugins.fold<_, R?>(initial) { acc, it -> it.block(acc ?: return@plugins acc) }
    }
