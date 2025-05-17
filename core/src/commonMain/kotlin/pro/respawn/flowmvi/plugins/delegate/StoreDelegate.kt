package pro.respawn.flowmvi.plugins.delegate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.state
import pro.respawn.flowmvi.plugins.initPlugin
import pro.respawn.flowmvi.plugins.whileSubscribedPlugin

public class StoreDelegate<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    private val mode: DelegationMode,
    internal val delegate: Store<S, I, A>,
) {

    public val name: String = "${delegate.name.orEmpty()}StoreDelegate"

    // lazy to  init with the most recent state on first invocation
    @OptIn(DelicateStoreApi::class)
    private val _state by lazy { MutableStateFlow(delegate.state) }
    public val state: Flow<S> by lazy { _state.asStateFlow() }

    private inline fun CoroutineScope.subscribeChild(consume: FlowCollector<A>?) = with(delegate) {
        subscribe {
            coroutineScope {
                launch { states.collect(_state) }
                consume?.let { launch { actions.collect(it) } }
                awaitCancellation()
            }
        }
    }

    internal fun <DS : MVIState, DI : MVIIntent, DA : MVIAction> asPlugin(
        name: String? = this.name,
        consume: (suspend (A) -> Unit)?,
    ): StorePlugin<DS, DI, DA> = when (mode) {
        is DelegationMode.Immediate -> initPlugin(name) { subscribeChild(consume) }
        is DelegationMode.WhileSubscribed -> whileSubscribedPlugin(
            minSubscriptions = mode.minSubs,
            name = name,
        ) { subscribeChild(consume) }
    }

    // region junk
    override fun toString(): String = name
    override fun hashCode(): Int = delegate.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoreDelegate<*, *, *>) return false
        if (delegate != other.delegate) return false
        return true
    }
    // endregion
}
