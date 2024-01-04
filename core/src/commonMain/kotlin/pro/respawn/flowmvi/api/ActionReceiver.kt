package pro.respawn.flowmvi.api

/**
 * An entity that can receive different actions and then possibly emit them as an [ActionProvider].
 * Actions are collected by the [ActionConsumer].
 * This is most often implemented by a [Store] and exposed through [PipelineContext]
 */
public interface ActionReceiver<in A : MVIAction> {

    /**
     * Alias for [action] with one difference -
     * this function will launch a new coroutine to send the intent in background.
     *
     * The launched coroutine may suspend to
     * wait for the buffer to become available based on [ActionShareBehavior].
     *
     * @see action
     */
    @DelicateStoreApi
    public fun send(action: A)

    /**
     * Send a new side-effect to be processed by subscribers, only once.
     * How actions will be distributed and handled depends on [ActionShareBehavior].
     * Actions that make the capacity overflow may be dropped or the function may suspend until the buffer is freed.
     */
    public suspend fun action(action: A)
}
