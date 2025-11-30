package pro.respawn.flowmvi.api

import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.StorePluginBuilder
import pro.respawn.flowmvi.dsl.plugin

/**
 * A unit that can extend the business logic of the [Store].
 * All stores are mostly based on plugins, and their behavior is entirely determined by them.
 *
 * Plugins can influence subscription, stopping, and all other forms of store behavior.
 * Access the store's context and other functions through the [PipelineContext] receiver.
 * Plugins are typically made using [StorePluginBuilder].
 *
 * It is not recommended to implement this interface, instead, use one of the [plugin] builders
 */
@Suppress("ComplexInterface", "TooManyFunctions")
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface StorePlugin<S : MVIState, I : MVIIntent, A : MVIAction> : LazyPlugin<S, I, A> {

    /**
     * The name of this plugin. The name can be used for logging purposes, but most importantly, to
     * distinguish between different plugins.
     * Name is optional, in which case the plugins will be compared **by reference**.
     * If you attempt to [StoreBuilder.install] the same plugin, or different plugins
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
     * This callback is **not** invoked at all when state is changed through [StateReceiver.updateStateImmediate]
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
     * Invoked immediately before an [MVIIntent] is enqueued (dispatched into the intent buffer).
     * * Return null to drop the intent before it is buffered.
     * * Return another intent to replace it.
     * * Return [intent] to enqueue unchanged.
     * * If you drop the intent here, [onIntent] will **not** receive it.
     * * Exceptions thrown here bypass [onException] and will be thrown to the caller.
     */
    public fun onIntentEnqueue(intent: I): I? = intent

    /**
     * Invoked after an [MVIAction] is dequeued and before it is delivered to subscribers.
     * * Return null to drop the action.
     * * Return another action to replace it.
     * * Return [action] to continue unchanged.
     * * Exceptions thrown here bypass [onException] and will be thrown to the caller.
     */
    public fun onActionDispatch(action: A): A? = action

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
     * * If you drop an action here, [onActionDispatch] will **not** receive it.
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
     * * You can **prevent the launching of the store** by calling [PipelineContext.close], but this will not throw an
     *   exception, the store will just not work.
     * * Plugins that use `onSubscribe` will also not get their events until this is run.
     */
    public suspend fun PipelineContext<S, I, A>.onStart(): Unit = Unit

    /**
     * A callback to be executed each time [Store.subscribe] is called.
     *
     * * This callback is executed **after** the subscriber count is incremented, i.e. the value represents
     *   the **new** number of subscribers.
     * * There is no guarantee that the subscribers will not be able to subscribe when the store has not been started yet.
     *   But this function will be invoked as soon as the store is started, with the most recent subscriber count.
     * * This function is invoked in the store's scope, not the subscriber's scope.
     * * There is no guarantee that this will be invoked exactly before a subscriber reappears.
     *   It may be so that a second subscriber, for example,
     *   appears before the first one disappears (due to the parallel nature of
     *   coroutines). In that case, [onSubscribe] will be invoked first as if it was a second subscriber, and then
     *   [onUnsubscribe] will be invoked, as if there were more subscribers for a moment.
     * * Suspending in this function will prevent other plugins from receiving the subscription event (i.e. next plugins
     *   that use [onSubscribe] will wait for this one to complete.
     */
    public suspend fun PipelineContext<S, I, A>.onSubscribe(newSubscriberCount: Int): Unit = Unit

    /**
     * A callback to be executed when the subscriber cancels its subscription job (unsubscribes).
     *
     * * This callback is executed **after** the subscriber has been removed and **after** [subscriberCount] is
     *   decremented, i.e. the value represents the **new** number of subscribers.
     * * There is no guarantee that this will be invoked exactly before a subscriber reappears.
     *   It may be so that a second subscriber appears before the first one disappears (due to the parallel nature of
     *   coroutines). In that case, [onSubscribe] will be invoked first as if it was a second subscriber, and then
     *   [onUnsubscribe] will be invoked, as if there were more subscribers for a moment.
     * * Suspending in this function will prevent other plugins from receiving the subscription event (i.e. next plugins
     *   that use [onUnsubscribe] will wait for this one to complete.
     */
    public suspend fun PipelineContext<S, I, A>.onUnsubscribe(newSubscriberCount: Int): Unit = Unit

    /**
     * Invoked when [Store.close] is invoked.
     *
     * * This is called **after** the store is already closed, and you cannot influence the outcome.
     * * This is invoked for both exceptional stops and normal stops.
     * * Will not be invoked when an [Error] is thrown.
     *
     * ### Warning:
     * This function is called in an undefined coroutine context on a random thread,
     * when the pipeline is already canceled. It should be fast and non-blocking.
     *
     * @param e the exception the store is closed with. Can be null for normal completions.
     * For everything except [kotlinx.coroutines.CancellationException]s, will not be null.
     */
    public fun ShutdownContext<S, I, A>.onStop(e: Exception?): Unit = Unit

    /**
     * Called when an intent is not delivered to the store.
     *
     * This can happen according to the [Channel]'s documentation:
     * * When the store has a limited buffer and it overflows.
     * * When store is stopped before this event could be handled, or while it is being handled.
     * * When the [onIntent] function throws an exception that is not handled by the [onException] block.
     * * When the store is stopped and there were intents in the buffer, in which case, `onUndeliveredIntent` will
     * be called on all of them.
     *
     * ### Warning:
     * This function is called in an undefined coroutine context on a random thread,
     * while the store is running or already stopped. It should be fast, non-blocking,
     * and must **not throw exceptions**, or the store will crash. The [onException] block will **not** handle
     * exceptions in this function.
     */
    public fun ShutdownContext<S, I, A>.onUndeliveredIntent(intent: I): Unit = Unit

    /**
     * Called when an action is not delivered to the store.
     *
     * This can happen according to the [Channel]'s documentation:
     * * When the Store's [ActionShareBehavior] is [ActionShareBehavior.Distribute] or [ActionShareBehavior.Restrict].
     * In this case, depending on the configuration, the queue of actions may have a limited buffer and overflow.
     * * When store is stopped before this event could be received by subscribers.
     * * When the subscriber cancels their subscription or throws before it could process the action.
     * * When the store is stopped and there were actions in the buffer, in which case, `onUndeliveredAction` will
     * be called on all of them.
     *
     * ### Warning:
     * This function is called in an undefined coroutine context on a random thread,
     * while the store is running or already stopped. It should be fast, non-blocking,
     * and must **not throw exceptions**, or the store will crash. The [onException] block will **not** handle
     * exceptions in this function.
     */
    public fun ShutdownContext<S, I, A>.onUndeliveredAction(action: A): Unit = Unit

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
    override fun toString(): String
    override fun invoke(config: StoreConfiguration<S>): StorePlugin<S, I, A> = this
}
