package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope

/**
 * A unit that can extend the business logic of the [Store].
 * All stores are mostly based on plugins, and their behavior is entirely determined by them.
 *
 * Plugins can influence subscription, stopping, and all other forms of store behavior.
 * Access the store's context and other functions through the [PipelineContext] receiver.
 * Plugins are typically made using [pro.respawn.flowmvi.dsl.StorePluginBuilder].
 *
 * It is not recommended to implement this interface,
 * if you really need to, subclass [pro.respawn.flowmvi.plugins.AbstractStorePlugin] instead.
 * If you do override this interface, you **must** comply with the contract defined above.
 */
@Suppress("ComplexInterface")
public interface StorePlugin<S : MVIState, I : MVIIntent, A : MVIAction> {

    /**
     * The name of this plugin. The name can be used for logging purposes, but most importantly, to
     * distinguish between different plugins.
     * Name is optional, in which case the plugins will be compared **by reference**.
     * If you attempt to [pro.respawn.flowmvi.dsl.StoreBuilder.install] the same plugin, or different plugins
     * with the same name, multiple times, **an exception will be thrown**.
     * If you need to have the same plugin installed multiple times, consider giving plugins different names.
     * Plugins that have no name can be installed multiple times, assuming they are different instances of a plugin.
     * Consider the following examples:
     * ```
     * loggingPlugin("foo")
     * analyticsPlugin("foo") // -> will throw
     *
     * loggingPlugin(null)
     * analyticsPlugin(null) // -> OK
     *
     * loggingPlugin("plugin1")
     * loggingPlugin("plugin1") // -> will throw
     *
     * loggingPlugin("plugin1")
     * loggingPlugin("plugin2") // -> OK, but same logs will be printed twice
     *
     * loggingPlugin(null)
     * loggingPlugin(null) // -> OK, but same logs will be printed twice
     *
     * val plugin = loggingPlugin(null)
     * install(plugin)
     * install(plugin) // -> will throw
     * ```
     */
    public val name: String?

    /**
     * A callback to be invoked each time [StateReceiver.updateState] or [StateReceiver.withState] is called.
     * This callback is invoked **before** the state changes, and any plugin can veto (forbid)
     * or modify the state change.
     *
     * This callback is invoked **after** a [StateReceiver.updateState] call is finished.
     * This callback is **not** invoked at all when state is changed through [StateReceiver.useState]
     * or when [StateProvider.state] is obtained.
     *
     *  * Return null to cancel the state change. All plugins registered later when building the store will not receive
     * this event.
     *  * Return [new] to continue the chain of modification, or allow the state to change,
     *  if no other plugins change it.
     *  * Return [old] to veto the state change, but allow next plugins in the queue to process the state.
     *  * Execute other operations using [PipelineContext].
     */
    public suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = new

    /**
     * A callback which is invoked each time an [MVIIntent] is received **and then begun** to be processed.
     * This callback is invoked **after** the intent is sent, sometimes after significant time if the store was stopped
     * or even **never** if the store's buffer overflows or store is not ever used again.
     * * Return null to veto the processing and prevent other plugins from using the [intent].
     * * Return another intent to replace [intent] with another one and continue with the chain.
     * * Return [intent] to continue processing, leaving the [intent] unmodified.
     * * Execute other operations using [PipelineContext].
     */
    public suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = intent

    /**
     * A callback that is invoked each time an [MVIAction] has been sent.
     * This is invoked **after** the action has been sent, but **before** the [ActionConsumer] handles it.
     * This function will always be invoked, even after the action is later dropped because of [ActionShareBehavior],
     * and it will be invoked before the [ActionReceiver.send] returns, if it has been suspended.
     * * Return null to veto the processing and prevent other plugins from using the [action].
     * * Return another action to replace [action] with another one and continue with the chain.
     * * Return [action] to continue processing, leaving the it unmodified.
     * * Execute other operations using [PipelineContext]
     */
    public suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = action

    /**
     * A callback that is invoked when [Store] handles an exception.
     * This is invoked **before** the exception is rethrown or otherwise processed.
     * This is invoked **asynchronously in a background job** and after the job that has thrown was cancelled, meaning
     * that some time may pass after the job is cancelled and the exception is handled.
     * Handled exceptions do not result in [Store.close].
     * * Return null to signal that the exception has been handled and recovered from, continuing the flow's processing.
     * * Return [e] if the exception was **not** handled and should be passed to other plugins.
     * * Execute other operations using [PipelineContext].
     *
     * If none of the plugins handles the exception (returns null), **the exception is rethrown and the store fails**.
     * Register a [pro.respawn.flowmvi.plugins.recoverPlugin] to recover from all exceptions.
     */
    public suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = e

    /**
     * A callback that is invoked **each time** the [Store.start] is called.
     * * Execute any operations using [PipelineContext].
     * * You can **prevent the launching of the store** by calling [PipelineContext.cancel], but this will not throw an
     * exception, the store will just not work.
     */
    public suspend fun PipelineContext<S, I, A>.onStart(): Unit = Unit

    /**
     * A callback to be executed each time [Store.subscribe] is called.
     * This callback is executed **before** the subscriber gets access to the store and **before** the [subscriberCount]
     * is incremented. This means, for the first subscription, [subscriberCount] will be zero.
     *
     * This function is invoked in the store's scope, not the subscriber's scope.
     * To launch jobs in the subscriber's scope, use [subscriberScope]. They will be canceled when the subscriber
     * unsubscribes
     * Execute any operations using [PipelineContext].
     */
    public fun PipelineContext<S, I, A>.onSubscribe(
        subscriberScope: CoroutineScope,
        subscriberCount: Int
    ): Unit = Unit

    /**
     * A callback to be executed when the subscriber cancels its subscription job (unsubscribes).
     * This callback is executed **after** the subscriber has been removed and **after** [subscriberCount] is
     * decremented. This means, for the last subscriber, the count will be 0.
     */
    public fun PipelineContext<S, I, A>.onUnsubscribe(subscriberCount: Int): Unit = Unit

    /**
     * Invoked when [Store.close] is invoked. This is called **after** the store is already closed, and you cannot
     * influence the outcome. This is invoked for both exceptional stops and normal stops.
     * Will not be invoked when an [Error] is thrown
     *
     * @param e the exception the store is closed with. Can be null for normal completions.
     * For everything except [kotlinx.coroutines.CancellationException]s, will not be null.
     */
    public fun onStop(e: Exception?): Unit = Unit

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}
