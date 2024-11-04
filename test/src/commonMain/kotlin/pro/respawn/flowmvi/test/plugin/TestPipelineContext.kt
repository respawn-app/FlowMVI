@file:OptIn(DelicateStoreApi::class)

package pro.respawn.flowmvi.test.plugin

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.ExperimentalStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle
import pro.respawn.flowmvi.test.TestStoreLifecycle
import pro.respawn.flowmvi.test.ensureStarted

@OptIn(ExperimentalStoreApi::class)
internal class TestPipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    override val config: StoreConfiguration<S>,
    val plugin: StorePlugin<S, I, A>,
) : PipelineContext<S, I, A>, StoreLifecycle by TestStoreLifecycle(config.coroutineContext[Job]) {

    override val coroutineContext by config::coroutineContext

    override var state: S by atomic(config.initial)
        private set

    @DelicateStoreApi
    override fun send(action: A) {
        ensureStarted()
        launch { with(plugin) { onAction(action) } }
    }

    override suspend fun action(action: A) {
        ensureStarted()
        with(plugin) { onAction(action) }
    }

    override suspend fun emit(intent: I): Unit = with(plugin) {
        ensureStarted()
        onIntent(intent)
    }
    override fun intent(intent: I) {
        ensureStarted()
        launch { emit(intent) }
    }

    override suspend fun updateState(transform: suspend S.() -> S) = with(plugin) {
        ensureStarted()
        onState(state, state.transform())?.also { state = it }
        Unit
    }

    override suspend fun withState(block: suspend S.() -> Unit) {
        ensureStarted()
        block(state)
    }

    override fun updateStateImmediate(block: S.() -> S) {
        ensureStarted()
        state = block(state)
    }
}
