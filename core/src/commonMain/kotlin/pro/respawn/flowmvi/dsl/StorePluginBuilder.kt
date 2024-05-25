package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.util.setOnce

/**
 * A class that builds a new [StorePlugin]
 * For more documentation, see [StorePlugin]
 *
 * Builder methods will throw [IllegalArgumentException] if they are assigned multiple times. Each plugin can only
 * have **one** block per each type of [StorePlugin] callback.
 */
@FlowMVIDSL
public open class StorePluginBuilder<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor() {

    private var intent: (suspend PipelineContext<S, I, A>.(I) -> I?)? = null
    private var state: (suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?)? = null
    private var action: (suspend PipelineContext<S, I, A>.(A) -> A?)? = null
    private var exception: (suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?)? = null
    private var start: (suspend PipelineContext<S, I, A>.() -> Unit)? = null
    private var subscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null
    private var unsubscribe: (suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit)? = null
    private var stop: ((e: Exception?) -> Unit)? = null

    /**
     * @see [StorePlugin.name]
     */
    @FlowMVIDSL
    public var name: String? = null

    /**
     * @see [StorePlugin.onIntent]
     */
    @FlowMVIDSL
    public fun onIntent(block: suspend PipelineContext<S, I, A>.(intent: I) -> I?): Unit = setOnce(::intent, block)

    /**
     * @see [StorePlugin.onState]
     */
    @FlowMVIDSL
    public fun onState(block: suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?): Unit = setOnce(::state, block)

    /**
     * @see [StorePlugin.onStart]
     */
    @FlowMVIDSL
    public fun onStart(block: suspend PipelineContext<S, I, A>.() -> Unit): Unit = setOnce(::start, block)

    /**
     * @see [StorePlugin.onStop]
     */
    @FlowMVIDSL
    public fun onStop(block: (e: Exception?) -> Unit): Unit = setOnce(::stop, block)

    /**
     * @see [StorePlugin.onException]
     */
    @FlowMVIDSL
    public fun onException(
        block: suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?
    ): Unit = setOnce(::exception, block)

    /**
     * @see [StorePlugin.onAction]
     */
    @FlowMVIDSL
    public fun onAction(block: suspend PipelineContext<S, I, A>.(action: A) -> A?): Unit = setOnce(::action, block)

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

    @FlowMVIDSL
    @PublishedApi
    internal fun build(): StorePlugin<S, I, A> = object : StorePlugin<S, I, A> {
        override val name = this@StorePluginBuilder.name
        override suspend fun PipelineContext<S, I, A>.onStart() {
            this@StorePluginBuilder.start?.invoke(this)
        }

        override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? {
            val block = this@StorePluginBuilder.state ?: return new
            return block(old, new)
        }

        override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? {
            val block = this@StorePluginBuilder.intent ?: return intent
            return block(intent)
        }

        override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? {
            val block = this@StorePluginBuilder.action ?: return action
            return block(action)
        }

        override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? {
            val block = this@StorePluginBuilder.exception ?: return e
            return block(e)
        }

        override suspend fun PipelineContext<S, I, A>.onSubscribe(newSubscriberCount: Int) {
            this@StorePluginBuilder.subscribe?.invoke(this, newSubscriberCount)
        }

        override suspend fun PipelineContext<S, I, A>.onUnsubscribe(newSubscriberCount: Int) {
            this@StorePluginBuilder.unsubscribe?.invoke(this, newSubscriberCount)
        }

        override fun onStop(e: Exception?) {
            stop?.invoke(e)
        }

        override fun toString(): String = name?.let { "StorePlugin \"$it\"" } ?: super.toString()

        override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()

        override fun equals(other: Any?): Boolean = when {
            other !is StorePlugin<*, *, *> -> false
            other.name == null && name == null -> this === other
            else -> name == other.name
        }
    }
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
