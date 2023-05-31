package pro.respawn.flowmvi.plugin

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.base.PipelineContext

internal open class CompositePlugin
<S : MVIState, I : MVIIntent, C : PipelineContext<S, I>, P : BaseStorePlugin<S, I, C>>(
    private vararg val plugins: P,
) : BaseStorePlugin<S, I, C> {

    override val name: String get() = "CompositeStorePlugin"

    override fun C.onStart() = plugins { onStart() }

    override fun C.onState(state: S): S? = plugins(state) { onState(it) }

    override fun C.onIntent(intent: I): I? = plugins(intent) { onIntent(it) }

    override fun C.onException(e: Exception): Exception? = plugins(e) { onException(it) }

    override fun onStop() = plugins { onStop() }

    private inline fun plugins(block: P.() -> Unit) = plugins.forEach(block)

    private inline fun <R> plugins(
        initial: R,
        block: P.(R) -> R?
    ) = plugins.fold<_, R?>(initial) { acc, it -> it.block(acc ?: return@plugins acc) }
}
