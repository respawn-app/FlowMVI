package pro.respawn.flowmvi.action

/**
 * An class representing how [MVIAction] sharing will be handled in the [MVIStore].
 * There are 3 possible behaviors, which will be different depending on the use-case.
 * When in doubt, use the default one, and change if you have issues.
 * @see MVIStore
 */
public sealed interface ActionShareBehavior {

    /**
     * Actions will be distributed to all subscribers equally. Each subscriber will receive a reference to a single
     * instance of the action that was sent from any store. Use when you want to have multiple subscribers
     * that each consume actions. Be aware that, however, if there's at least one subscriber, they will consume an
     * action entirely (i.e. other subscribers won't receive it when they "return" if they weren't present at the
     * time of emission).
     * @param buffer How many actions will be buffered when consumer processes them slower than they are emitted
     * @param replay How many actions will be replayed to each new subscriber
     */
    public data class Share(val buffer: Int = DefaultBufferSize, val replay: Int = 0) : ActionShareBehavior

    /**
     * Fan-out behavior means that multiple subscribers are allowed,
     * and each action will be distributed to one subscriber.
     * If there are multiple subscribers, only one of them will handle an instance of an action,
     * and **the order is unspecified**.
     *
     * **This is the default**.
     *
     * @param buffer How many actions will be buffered when consumer processes them slower than they are emitted
     */
    public data class Distribute(val buffer: Int = DefaultBufferSize) : ActionShareBehavior

    /**
     * Restricts the count of subscribers to 1.
     * Attempting to subscribe to a store that has already been subscribed to will result in an exception.
     * In other words, you will be required to create a new store for each invocation of [subscribe].
     *
     * **Repeated subscriptions are not allowed too, including lifecycle-aware collection**.
     *
     * @param buffer How many actions will be buffered when consumer processes them slower than they are emitted
     */
    public data class Restrict(val buffer: Int = DefaultBufferSize) : ActionShareBehavior

    public companion object {

        /**
         * The default action buffer size
         * @see kotlinx.coroutines.channels.Channel.BUFFERED
         */
        public const val DefaultBufferSize: Int = 64
    }
}
