package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateStrategy
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.logging.NoOpStoreLogger
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A builder class for creating [StoreConfiguration]s.
 */
@FlowMVIDSL
public class StoreConfigurationBuilder @PublishedApi internal constructor() {

    /**
     * Declare that [MVIIntent]s must be processed in parallel.
     * All guarantees on the order of [MVIIntent]s will be lost.
     * Intents may still be dropped according to [onOverflow].
     * Intents are not **obtained** in parallel, just processed.
     *
     * `false` by default.
     */
    @FlowMVIDSL
    public var parallelIntents: Boolean = false

    /**
     * Provide the [ActionShareBehavior] for the store.
     * For stores where actions are of type [Nothing] this must be set to [ActionShareBehavior.Disabled].
     * Will be set automatically when using the two-argument store builder.
     *
     * [ActionShareBehavior.Distribute] by default.
     */
    @FlowMVIDSL
    public var actionShareBehavior: ActionShareBehavior = ActionShareBehavior.Distribute()

    /**
     * Configure the [StateStrategy] of this [Store].
     *
     * Available strategies:
     * * [StateStrategy.Atomic]
     * * [StateStrategy.Immediate]
     *
     * Make sure to read the documentation of the strategy before modifying this property.
     *
     * [StateStrategy.Atomic] with [StateStrategy.Atomic.reentrant] = `true` by default
     */
    @FlowMVIDSL
    public var stateStrategy: StateStrategy = StateStrategy.Atomic(true)

    /**
     * Designate the maximum capacity of [MVIIntent]s waiting for processing
     * in the [pro.respawn.flowmvi.api.IntentReceiver]'s queue.
     * Intents that overflow this capacity will be processed according to [onOverflow].
     * This should be either a positive value, or one of:
     *  * [Channel.UNLIMITED]
     *  * [Channel.CONFLATED]
     *  * [Channel.RENDEZVOUS]
     *  * [Channel.BUFFERED]
     *
     *  [Channel.UNLIMITED] by default.
     */
    @FlowMVIDSL
    public var intentCapacity: Int = Channel.UNLIMITED

    /**
     * Designate behavior for when [IntentReceiver]'s [MVIIntent] pool overflows.
     *
     * [BufferOverflow.DROP_OLDEST] by default
     */
    @FlowMVIDSL
    public var onOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST

    /**
     * Settings this to `true` enables additional [Store] validations and debug logging.
     */
    @FlowMVIDSL
    public var debuggable: Boolean = false

    /**
     * A flag to indicate that clients may subscribe to this store even while it is not started.
     * If you intend to stop and restart your store while the subscribers are present, set this to `true`.
     */
    @FlowMVIDSL
    public var allowIdleSubscriptions: Boolean? = null

    /**
     *  A coroutine context overrides for the [Store].
     *  This context will be merged with the one the store was launched with (e.g. `viewModelScope`).
     *
     *  All store operations will be launched in that context by default
     */
    @FlowMVIDSL
    public var coroutineContext: CoroutineContext = EmptyCoroutineContext

    /**
     * [StoreLogger] used for this store.
     * If [debuggable] is `true` and logger has not been set,
     * then [PlatformStoreLogger] will be used, else [NoOpStoreLogger] will be used.
     *
     * If the logger was set explicitly, then it will be used regardless of the [debuggable] flag.
     */
    @FlowMVIDSL
    public var logger: StoreLogger? = null

    /**
     * Signals to plugins that they should enable their own verification logic.
     *
     * By default, set to `true` only if the store is [debuggable].
     */
    @FlowMVIDSL
    public var verifyPlugins: Boolean? = null

    /**
     * Set the future name of the store.
     * See [Store.name] for more info.
     *
     * `null` by default
     */
    @FlowMVIDSL
    public var name: String? = null

    // region deprecated
    @Deprecated(
        "Please use the StateStrategy property",
        replaceWith = ReplaceWith("stateStrategy = StateStrategy.Atomic()"),
    )
    @FlowMVIDSL
    @Suppress("UndocumentedPublicProperty")
    public var atomicStateUpdates: Boolean
        get() = stateStrategy is StateStrategy.Atomic
        set(value) {
            stateStrategy = if (value) StateStrategy.Atomic(true) else StateStrategy.Immediate
        }
    // endregion

    /**
     * Create the [StoreConfiguration]
     */
    @PublishedApi
    internal operator fun <S : MVIState> invoke(initial: S): StoreConfiguration<S> = StoreConfiguration(
        initial = initial,
        parallelIntents = parallelIntents,
        actionShareBehavior = actionShareBehavior,
        intentCapacity = intentCapacity,
        onOverflow = onOverflow,
        debuggable = debuggable,
        coroutineContext = coroutineContext,
        logger = logger ?: if (debuggable) PlatformStoreLogger else NoOpStoreLogger,
        name = name,
        allowIdleSubscriptions = allowIdleSubscriptions ?: !debuggable,
        verifyPlugins = verifyPlugins ?: debuggable,
        stateStrategy = stateStrategy,
    )
}

/**
 * Create a new [StoreConfiguration]
 */
@FlowMVIDSL
public inline fun <S : MVIState> configuration(
    initial: S,
    block: StoreConfigurationBuilder.() -> Unit,
): StoreConfiguration<S> = StoreConfigurationBuilder().apply(block).invoke(initial)
