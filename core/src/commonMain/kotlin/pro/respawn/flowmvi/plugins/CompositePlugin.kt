package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin

internal class CompositePlugin<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    name: String = DefaultName,
    private val plugins: Map<String, StorePlugin<S, I, A>>,
) : AbstractStorePlugin<S, I, A>(name) {

    internal constructor(
        name: String = DefaultName,
        vararg plugins: StorePlugin<S, I, A>
    ) : this(name, plugins.associateBy { it.name })

    internal constructor(
        name: String = DefaultName,
        plugins: Iterable<StorePlugin<S, I, A>>
    ) : this(name, plugins.associateBy { it.name })

    override suspend fun PipelineContext<S, I, A>.onStart(): Unit = plugins { onStart() }
    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = plugins(old) { onState(old, it) }
    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = plugins(intent) { onIntent(it) }
    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = plugins(action) { onAction(it) }
    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = plugins(e) { onException(it) }
    override suspend fun PipelineContext<S, I, A>.onSubscribe() = plugins { onSubscribe() }
    internal fun PipelineContext<S, I, A>.onSubscribeParallel() = pluginsParallel { onSubscribe() }
    override fun onStop(): Unit = plugins { onStop() }

    private inline fun plugins(block: StorePlugin<S, I, A>.() -> Unit) = plugins.values.forEach(block)
    private inline fun <R> plugins(
        initial: R,
        block: StorePlugin<S, I, A>.(R) -> R?
    ) = plugins.values.fold<_, R?>(initial) { acc, it -> it.block(acc ?: return@plugins acc) }

    private fun CoroutineScope.pluginsParallel(block: suspend StorePlugin<S, I, A>.() -> Unit) =
        plugins.values.forEach { launch { block(it) } }

    override fun equals(other: Any?): Boolean {
        if (other !is CompositePlugin<*, *, *>) return false
        return name == other.name && plugins.keys == other.plugins.keys
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + plugins.keys.hashCode()
        return result
    }

    operator fun get(name: String) = plugins[name]

    companion object {

        const val DefaultName = "CompositePlugin"
    }
}
