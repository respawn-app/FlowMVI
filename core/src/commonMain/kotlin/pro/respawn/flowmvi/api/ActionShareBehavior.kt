package pro.respawn.flowmvi.api

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.channels.BufferOverflow

/**
 * An class representing how [MVIAction] sharing will be handled in the [ActionProvider].
 * There are 4 possible behaviors, which will be different depending on your use-case.
 * When in doubt, use the default one, and change if you have issues.
 * @see ActionProvider
 * @see ActionReceiver
 * @see ActionConsumer
 */
@Immutable
public sealed interface ActionShareBehavior {

    /**
     * Actions will be distributed to all subscribers equally. Each subscriber will receive a reference to a single
     * instance of the action that was sent from any store. Use when you want to have multiple subscribers
     * that each consume actions. Be aware that, however, if there's at least one subscriber, they will consume an
     * action entirely (i.e. other subscribers won't receive it when they "return" if they weren't present at the
     * time of emission).
     * @param buffer How many actions will be buffered when consumer processes them slower than they are emitted
     * @param replay How many actions will be replayed to each new subscriber
     * @param overflow How actions that overflow the [buffer] are handled.
     * @see [kotlinx.coroutines.channels.BufferOverflow]
     */
    public data class Share(
        val buffer: Int = DefaultBufferSize,
        val replay: Int = 0,
        val overflow: BufferOverflow = BufferOverflow.SUSPEND,
    ) : ActionShareBehavior

    /**
     * Fan-out (distribute) behavior means that multiple subscribers are allowed,
     * and each action will be distributed to one subscriber.
     * If there are multiple subscribers, only one of them will handle an instance of an action,
     * and **the order is unspecified**.
     *
     * @param buffer How many actions will be buffered when consumer processes them slower than they are emitted
     * @param overflow How actions that overflow the [buffer] are handled.
     * @see [kotlinx.coroutines.channels.BufferOverflow]
     */
    public data class Distribute(
        val buffer: Int = DefaultBufferSize,
        val overflow: BufferOverflow = BufferOverflow.SUSPEND
    ) : ActionShareBehavior

    /**
     * Restricts the count of subscription events to 1.
     * Attempting to subscribe to a store that has already been subscribed to will result in an exception.
     * In other words, you will be required to create a new store for each invocation of [Store.subscribe].
     *
     * **Repeated subscriptions are not allowed, including lifecycle-aware collection**.
     *
     * @param buffer How many actions will be buffered when consumer processes them slower than they are emitted
     * @param overflow How actions that overflow the [buffer] are handled.
     * @see [kotlinx.coroutines.channels.BufferOverflow]
     */
    public data class Restrict(
        val buffer: Int = DefaultBufferSize,
        val overflow: BufferOverflow = BufferOverflow.SUSPEND
    ) : ActionShareBehavior

    /**
     * Designates that [MVIAction]s are disabled entirely.
     * Attempting to consume or send an action will throw.
     */
    public data object Disabled : ActionShareBehavior

    public companion object {

        /**
         * The default action buffer size
         * @see kotlinx.coroutines.channels.Channel.BUFFERED
         */
        public const val DefaultBufferSize: Int = 64
    }
}
