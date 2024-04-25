package pro.respawn.flowmvi.test.plugin

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin

internal class TestPipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    override val config: StoreConfiguration<S>,
    val plugin: StorePlugin<S, I, A>,
) : PipelineContext<S, I, A> {

    override val coroutineContext by config::coroutineContext

    var state: S by atomic(config.initial)
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

    override suspend fun withState(block: suspend S.() -> Unit): Unit = block(state)

    override fun useState(block: S.() -> S) {
        state = block(state)
    }
}
