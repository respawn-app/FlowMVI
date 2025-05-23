package pro.respawn.flowmvi.plugins.delegate

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.state
import pro.respawn.flowmvi.plugins.initPlugin
import pro.respawn.flowmvi.plugins.whileSubscribedPlugin
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * An object that provides access to the state of the [delegate] store.
 * Can be used with the [pro.respawn.flowmvi.plugins.delegate.delegate] Store dsl to obtain the state projection:
 *
 * ```kotlin
 * val store = store {
 *     val feedState by delegate(feedStore) {
 *         // handle actions
 *     }
 *     whileSubscribed {
 *         feedState.collect { state ->
 *             // use projection.
 *         }
 *     }
 * }
 * ```
 *
 * The StoreDelegate is responsible for projecting the state of the delegate store to the principal store
 * and optionally handling actions from the delegate store.
 *
 * Warning: [stateProjection] is not guaranteed to be up-to-date. See [DelegationMode] for more details.
 *
 * @property delegate The store that will be delegated to
 * @property mode The delegation mode that determines when and how the delegate's state is projected
 *
 * @see pro.respawn.flowmvi.plugins.delegate.delegate
 */
@OptIn(InternalFlowMVIAPI::class, DelicateStoreApi::class)
public class StoreDelegate<S : MVIState, I : MVIIntent, A : MVIAction> @InternalFlowMVIAPI constructor(
    internal val delegate: Store<S, I, A>,
    private val mode: DelegationMode = DelegationMode.Default,
) : ReadOnlyProperty<Any?, Flow<S>> {

    /**
     * The name of this delegate, derived from the delegate store's name.
     */
    public val name: String = "${delegate.name.orEmpty()}StoreDelegate"

    // lazy to init with the most recent state on first invocation (or never, based on mode)
    private val _state by lazy { MutableStateFlow(delegate.state) }

    /**
     * A flow that projects the state of the delegate store based on the delegation mode.
     *
     * - In [DelegationMode.Immediate] mode, this directly exposes the delegate store's states flow.
     * - In [DelegationMode.WhileSubscribed] mode, this exposes a state flow that is updated only when
     *   the principal store has subscribers.
     */
    public val stateProjection: Flow<S> by lazy {
        when (mode) {
            is DelegationMode.Immediate -> delegate.states
            is DelegationMode.WhileSubscribed -> _state.asStateFlow()
        }
    }

    /**
     * Subscribes to the delegate store and handles its state and actions.
     *
     * @param consume Optional collector for actions emitted by the delegate store
     */
    private inline fun <DS : MVIState, DI : MVIIntent, DA : MVIAction> PipelineContext<DS, DI, DA>.subscribeChild(
        noinline consume: ChildConsume<DS, DI, DA, A>?,
    ) = with(delegate) {
        subscribe {
            coroutineScope {
                when (mode) {
                    is DelegationMode.Immediate -> Unit // state is already always up-to-date
                    is DelegationMode.WhileSubscribed -> launch { states.collect(_state) }
                }
                consume?.let { block -> launch { actions.collect { block(it) } } }
                awaitCancellation()
            }
        }
    }

    internal inline fun <DS : MVIState, DI : MVIIntent, DA : MVIAction> asPlugin(
        name: String? = this.name,
        noinline consume: ChildConsume<DS, DI, DA, A>?
    ): StorePlugin<DS, DI, DA> = when (mode) {
        is DelegationMode.Immediate -> initPlugin(name) { subscribeChild(consume) }
        is DelegationMode.WhileSubscribed -> whileSubscribedPlugin(
            minSubscriptions = mode.minSubs,
            name = name,
        ) { subscribeChild(consume) }
    }

    // region junk
    override fun getValue(thisRef: Any?, property: KProperty<*>): Flow<S> = stateProjection
    override fun toString(): String = name
    override fun hashCode(): Int = delegate.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoreDelegate<*, *, *>) return false
        if (delegate != other.delegate) return false
        return true
    }
}

internal typealias ChildConsume <S, I, A, CA> = (suspend PipelineContext<S, I, A>.(CA) -> Unit)
