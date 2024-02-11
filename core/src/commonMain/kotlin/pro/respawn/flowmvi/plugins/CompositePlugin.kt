package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin

/**
 * A plugin that delegates to [plugins] in the iteration order.
 * This is an implementation of the "Composite" pattern and the "Chain or Responsibility" pattern.
 *
 * This plugin is mostly not intended for usage in general code as there are no real use cases for it so far.
 * It can be useful in testing and custom store implementations.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> compositePlugin(
    plugins: Set<StorePlugin<S, I, A>>,
    name: String? = null,
): StorePlugin<S, I, A> = CompositePlugin(plugins, name)

private class CompositePlugin<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val plugins: Set<StorePlugin<S, I, A>>,
    name: String? = null,
) : AbstractStorePlugin<S, I, A>(name) {

    override suspend fun PipelineContext<S, I, A>.onStart(): Unit = plugins { onStart() }
    override fun onStop(e: Exception?): Unit = plugins { onStop(e) }
    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = plugins(new) { onState(old, it) }
    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = plugins(intent) { onIntent(it) }
    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = plugins(action) { onAction(it) }
    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = plugins(e) { onException(it) }
    override suspend fun PipelineContext<S, I, A>.onUnsubscribe(subscriberCount: Int): Unit =
        plugins { onUnsubscribe(subscriberCount) }

    override suspend fun PipelineContext<S, I, A>.onSubscribe(
        subscriberCount: Int
    ): Unit = plugins { onSubscribe(subscriberCount) }

    private inline fun plugins(block: StorePlugin<S, I, A>.() -> Unit) = plugins.forEach(block)
    private inline fun <R> plugins(
        initial: R,
        block: StorePlugin<S, I, A>.(R) -> R?
    ) = plugins.fold<_, R?>(initial) { acc, it -> it.block(acc ?: return@plugins acc) }
}
