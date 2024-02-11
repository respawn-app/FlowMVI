package pro.respawn.flowmvi.test.plugin

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import kotlin.coroutines.CoroutineContext

public class TestPipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    initial: S,
    override val coroutineContext: CoroutineContext,
    public val plugin: StorePlugin<S, I, A>,
) : PipelineContext<S, I, A> {

    public var state: S by atomic(initial)
        private set

    public var closed: Boolean by atomic(false)
        private set

    @DelicateStoreApi
    override fun send(action: A) {
        launch { with(plugin) { onAction(action) } }
    }

    override suspend fun action(action: A) {
        with(plugin) { onAction(action) }
    }

    override suspend fun emit(intent: I): Unit = with(plugin) { onIntent(intent) }
    override fun intent(intent: I) {
        launch { emit(intent) }
    }

    override suspend fun updateState(transform: suspend S.() -> S) {
        with(plugin) {
            onState(state, state.transform())?.also { state = it }
        }
    }

    override fun close() {
        closed = true
    }

    override suspend fun withState(block: suspend S.() -> Unit): Unit = block(state)

    override fun useState(block: S.() -> S) {
        state = block(state)
    }
}
