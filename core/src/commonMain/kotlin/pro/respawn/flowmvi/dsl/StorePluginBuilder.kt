package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.util.setOnce

/**
 * A class that builds a new [StorePlugin]
 * For more documentation, see [StorePlugin]
 *
 * Builder methods will throw [IllegalArgumentException] if they are assigned multiple times. Each plugin can only
 * have **one** block per each type of [StorePlugin] callback.
 */
@FlowMVIDSL
@Suppress("TooManyFunctions") // intended
public open class StorePluginBuilder<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor() {

    private var intent: (suspend PipelineContext<S, I, A>.(I) -> I?)? = null
    private var intentEnqueue: ((I) -> I?)? = null
    private var state: (suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?)? = null
    private var action: (suspend PipelineContext<S, I, A>.(A) -> A?)? = null
    private var actionDispatch: ((A) -> A?)? = null
    private var exception: (suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?)? = null
    private var start: (suspend PipelineContext<S, I, A>.() -> Unit)? = null
    private var subscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null
    private var unsubscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null
    private var undeliveredIntent: (ShutdownContext<S, I, A>.(intent: I) -> Unit)? = null
    private var undeliveredAction: (ShutdownContext<S, I, A>.(action: A) -> Unit)? = null
    private var stop: (ShutdownContext<S, I, A>.(e: Exception?) -> Unit)? = null

    /**
     * See [StorePlugin.name]
     */
    @FlowMVIDSL
    public var name: String? = null

    /**
     * See [StorePlugin.onStart]
     */
    @FlowMVIDSL
    public fun onStart(block: suspend PipelineContext<S, I, A>.() -> Unit): Unit = ::start.setOnce(block)

    /**
     * See [StorePlugin.onState]
     */
    @FlowMVIDSL
    public fun onState(block: suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?): Unit = ::state.setOnce(block)

    /**
     * See [StorePlugin.onIntentEnqueue]
     */
    @FlowMVIDSL
    public fun onIntentEnqueue(block: (intent: I) -> I?): Unit = ::intentEnqueue.setOnce(block)

    /**
     * See [StorePlugin.onIntent]
     */
    @FlowMVIDSL
    public fun onIntent(block: suspend PipelineContext<S, I, A>.(intent: I) -> I?): Unit = ::intent.setOnce(block)

    /**
     * See [StorePlugin.onAction]
     */
    @FlowMVIDSL
    public fun onAction(block: suspend PipelineContext<S, I, A>.(action: A) -> A?): Unit = ::action.setOnce(block)

    /**
     * See [StorePlugin.onActionDispatch]
     */
    @FlowMVIDSL
    public fun onActionDispatch(block: (action: A) -> A?): Unit = ::actionDispatch.setOnce(block)

    /**
     * See [StorePlugin.onException]
     */
    @FlowMVIDSL
    public fun onException(
        block: suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?
    ): Unit = ::exception.setOnce(block)

    /**
     * See [StorePlugin.onSubscribe]
     */
    @FlowMVIDSL
    public fun onSubscribe(
        block: suspend PipelineContext<S, I, A>.(newSubscriberCount: Int) -> Unit
    ): Unit = ::subscribe.setOnce(block)

    /**
     * See [StorePlugin.onUnsubscribe]
     */
    @FlowMVIDSL
    public fun onUnsubscribe(
        block: suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit
    ): Unit = ::unsubscribe.setOnce(block)

    /**
     * See [StorePlugin.onStop]
     */
    @FlowMVIDSL
    public fun onStop(block: ShutdownContext<S, I, A>.(e: Exception?) -> Unit): Unit = ::stop.setOnce(block)

    /**
     * See [StorePlugin.onUndeliveredIntent]
     */
    @FlowMVIDSL
    @DelicateStoreApi
    public fun onUndeliveredIntent(
        block: ShutdownContext<S, I, A>.(intent: I) -> Unit
    ): Unit = ::undeliveredIntent.setOnce(block)

    /**
     * See [StorePlugin.onUndeliveredAction]
     */
    @FlowMVIDSL
    @DelicateStoreApi
    public fun onUndeliveredAction(
        block: ShutdownContext<S, I, A>.(action: A) -> Unit
    ): Unit = ::undeliveredAction.setOnce(block)

    @PublishedApi
    internal fun build(): PluginInstance<S, I, A> = PluginInstance(
        name = name,
        onStart = start,
        onState = state,
        onIntentEnqueue = intentEnqueue,
        onIntent = intent,
        onAction = action,
        onActionDispatch = actionDispatch,
        onException = exception,
        onSubscribe = subscribe,
        onUnsubscribe = unsubscribe,
        onUndeliveredIntent = undeliveredIntent,
        onUndeliveredAction = undeliveredAction,
        onStop = stop,
    )
}
