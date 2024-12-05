@file:OptIn(DelicateStoreApi::class)

package pro.respawn.flowmvi.test.plugin

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.test.TestStoreLifecycle
import pro.respawn.flowmvi.test.ensureStarted

@OptIn(ExperimentalFlowMVIAPI::class, NotIntendedForInheritance::class)
internal class TestPipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    override val config: StoreConfiguration<S>,
    val plugin: StorePlugin<S, I, A>,
) : PipelineContext<S, I, A>, StoreLifecycle by TestStoreLifecycle(config.coroutineContext[Job]) {

    override val coroutineContext by config::coroutineContext

    private var _state = atomic(config.initial)
    override val state by _state::value

    override fun compareAndSet(old: S, new: S): Boolean = _state.compareAndSet(old, new)

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
        updateStateImmediate { onState(state, state.transform()) ?: this }
    }

    override suspend fun withState(block: suspend S.() -> Unit) {
        ensureStarted()
        block(state)
    }
}
