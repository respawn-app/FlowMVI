package pro.respawn.flowmvi.api

/**
 * An entity that can receive different actions and then possibly emit them as an [ActionProvider].
 * Actions are collected by the [ActionConsumer].
 * This is most often implemented by a [Store] and exposed through [PipelineContext]
 */
public interface ActionReceiver<in A : MVIAction> {

    /**
     * Send a new side-effect to be processed by subscribers, only once.
     * How actions will be distributed and handled depends on [ActionShareBehavior].
     * Actions that make the capacity overflow may be dropped or the function may suspend until the buffer is freed.
     */
    public suspend fun send(action: A)

    /**
     * Alias for [send] for parity with [IntentReceiver.send]
     */
    public suspend fun emit(action: A): Unit = send(action)

    /**
     * Alias for [send]
     */
    public suspend fun action(action: A): Unit = send(action)
}
