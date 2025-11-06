@file:OptIn(DelicateStoreApi::class)

package pro.respawn.flowmvi.test.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.dsl.state
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.test.TestStoreLifecycle
import pro.respawn.flowmvi.test.ensureStarted

@OptIn(ExperimentalFlowMVIAPI::class, NotIntendedForInheritance::class, InternalFlowMVIAPI::class)
internal class TestPipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    override val config: StoreConfiguration<S>,
    scope: CoroutineScope,
    plugins: List<StorePlugin<S, I, A>>,
    name: String?,
    lifecycle: TestStoreLifecycle = TestStoreLifecycle(scope),
) : PipelineContext<S, I, A>, StoreLifecycle by lifecycle {

    private val _subs = MutableStateFlow(0)
    override val subscriberCount = _subs.asStateFlow()
    override val coroutineContext = lifecycle.coroutineContext + config.coroutineContext
    val plugin = SubscriptionHookedPlugin(plugins, name, _subs::value::set)

    private val _state = MutableStateFlow(config.initial)
    override val states: StateFlow<S> = _state.asStateFlow()
    override fun compareAndSet(old: S, new: S): Boolean = _state.compareAndSet(old, new)

    @DelicateStoreApi
    override fun send(action: A) {
        ensureStarted()
        launch { with(plugin) { onAction(action) } }
    }

    override suspend fun action(action: A) {
        ensureStarted()
        return plugin.run { onAction(action) }
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
        updateStateImmediate { onState(this, transform()) ?: this }
    }

    override suspend fun withState(block: suspend S.() -> Unit) {
        ensureStarted()
        block(state)
    }
}

private inline fun <S : MVIState, I : MVIIntent, A : MVIAction> SubscriptionHookedPlugin(
    delegates: List<StorePlugin<S, I, A>>,
    name: String?,
    crossinline onChange: (newValue: Int) -> Unit,
): StorePlugin<S, I, A> = compositePlugin(
    name = name,
    plugins = delegates + plugin {
        onSubscribe { onChange(it) }
        onUnsubscribe { onChange(it) }
    },
)
