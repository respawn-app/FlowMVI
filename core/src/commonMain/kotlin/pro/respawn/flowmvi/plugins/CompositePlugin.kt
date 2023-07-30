package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin

internal class CompositePlugin<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    private val plugins: Set<StorePlugin<S, I, A>>,
) : AbstractStorePlugin<S, I, A>(Name) {

    override suspend fun PipelineContext<S, I, A>.onStart(): Unit = plugins { onStart() }
    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = plugins(new) { onState(old, it) }
    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = plugins(intent) { onIntent(it) }
    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = plugins(action) { onAction(it) }
    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = plugins(e) { onException(it) }
    override suspend fun PipelineContext<S, I, A>.onSubscribe(subscriberCount: Int) =
        plugins { onSubscribe(subscriberCount) }

    override fun onStop(e: Exception?): Unit = plugins { onStop(e) }

    private inline fun plugins(block: StorePlugin<S, I, A>.() -> Unit) = plugins.forEach(block)
    private inline fun <R> plugins(
        initial: R,
        block: StorePlugin<S, I, A>.(R) -> R?
    ) = plugins.fold<_, R?>(initial) { acc, it -> it.block(acc ?: return@plugins acc) }

    companion object {

        // there may only be one composite plugin as it's intended for internal usage
        private const val Name = "CompositePlugin"
    }
}
