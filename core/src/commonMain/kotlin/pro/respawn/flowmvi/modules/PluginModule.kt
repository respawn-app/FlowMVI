package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.AbstractStorePlugin

internal class PluginModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val plugins: Set<StorePlugin<S, I, A>>,
) : AbstractStorePlugin<S, I, A>(null) {

    override suspend fun PipelineContext<S, I, A>.onStart(): Unit = plugins { onStart() }
    override fun onStop(e: Exception?): Unit = plugins { onStop(e) }
    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = plugins(new) { onState(old, it) }
    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = plugins(intent) { onIntent(it) }
    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = plugins(action) { onAction(it) }
    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = plugins(e) { onException(it) }
    override fun PipelineContext<S, I, A>.onUnsubscribe(
        subscriberCount: Int
    ) = plugins { onUnsubscribe(subscriberCount) }

    override fun PipelineContext<S, I, A>.onSubscribe(
        subscriberScope: CoroutineScope,
        subscriberCount: Int
    ) = plugins { onSubscribe(subscriberScope, subscriberCount) }

    private inline fun plugins(block: StorePlugin<S, I, A>.() -> Unit) = plugins.forEach(block)
    private inline fun <R> plugins(
        initial: R,
        block: StorePlugin<S, I, A>.(R) -> R?
    ) = plugins.fold<_, R?>(initial) { acc, it -> it.block(acc ?: return@plugins acc) }

    inline operator fun invoke(block: StorePlugin<S, I, A>.() -> Unit) = block()
}
