package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.api.context.UndeliveredHandlerContext
import pro.respawn.flowmvi.impl.PluginInstance
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
    private var state: (suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?)? = null
    private var action: (suspend PipelineContext<S, I, A>.(A) -> A?)? = null
    private var exception: (suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?)? = null
    private var start: (suspend PipelineContext<S, I, A>.() -> Unit)? = null
    private var subscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null
    private var unsubscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null
    private var undeliveredIntent: (UndeliveredHandlerContext<S, I, A>.(intent: I) -> Unit)? = null
    private var undeliveredAction: (UndeliveredHandlerContext<S, I, A>.(action: A) -> Unit)? = null
    private var stop: (ShutdownContext<S, I, A>.(e: Exception?) -> Unit)? = null

    /**
     * @see [StorePlugin.name]
     */
    @FlowMVIDSL
    public var name: String? = null

    /**
     * @see [StorePlugin.onStart]
     */
    @FlowMVIDSL
    public fun onStart(block: suspend PipelineContext<S, I, A>.() -> Unit): Unit = setOnce(::start, block)

    /**
     * @see [StorePlugin.onState]
     */
    @FlowMVIDSL
    public fun onState(block: suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?): Unit = setOnce(::state, block)

    /**
     * @see [StorePlugin.onIntent]
     */
    @FlowMVIDSL
    public fun onIntent(block: suspend PipelineContext<S, I, A>.(intent: I) -> I?): Unit = setOnce(::intent, block)

    /**
     * @see [StorePlugin.onAction]
     */
    @FlowMVIDSL
    public fun onAction(block: suspend PipelineContext<S, I, A>.(action: A) -> A?): Unit = setOnce(::action, block)

    /**
     * @see [StorePlugin.onException]
     */
    @FlowMVIDSL
    public fun onException(
        block: suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?
    ): Unit = setOnce(::exception, block)

    /**
     * @see [StorePlugin.onSubscribe]
     */
    @FlowMVIDSL
    public fun onSubscribe(
        block: suspend PipelineContext<S, I, A>.(newSubscriberCount: Int) -> Unit
    ): Unit = setOnce(::subscribe, block)

    /**
     * @see StorePlugin.onUnsubscribe
     */
    @FlowMVIDSL
    public fun onUnsubscribe(
        block: suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit
    ): Unit = setOnce(::unsubscribe, block)

    /**
     * @see [StorePlugin.onStop]
     */
    @FlowMVIDSL
    public fun onStop(block: ShutdownContext<S, I, A>.(e: Exception?) -> Unit): Unit = setOnce(::stop, block)

    @FlowMVIDSL
    public fun onUndeliveredIntent(
        block: UndeliveredHandlerContext<S, I, A>.(intent: I) -> Unit
    ): Unit = setOnce(::undeliveredIntent, block)

    @FlowMVIDSL
    public fun onUndeliveredAction(
        block: UndeliveredHandlerContext<S, I, A>.(action: A) -> Unit
    ): Unit = setOnce(::undeliveredAction, block)

    @PublishedApi
    internal fun build(): PluginInstance<S, I, A> = PluginInstance(
        name = name,
        onStart = start,
        onState = state,
        onIntent = intent,
        onAction = action,
        onException = exception,
        onSubscribe = subscribe,
        onUnsubscribe = unsubscribe,
        onUndeliveredIntent = undeliveredIntent,
        onUndeliveredAction = undeliveredAction,
        onStop = stop,
    )
}

/**
 * Build a new [StorePlugin] using [StorePluginBuilder].
 * See [StoreBuilder.install] to install the plugin automatically.
 * @see [StorePlugin]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> plugin(
    @BuilderInference builder: StorePluginBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = StorePluginBuilder<S, I, A>().apply(builder).build()
