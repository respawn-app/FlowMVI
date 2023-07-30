package pro.respawn.flowmvi.api

/**
 * An entity that can [render] states coming from a [StateProvider]. Most likely, a subscriber of the [Store].
 */
public fun interface StateConsumer<in S : MVIState> {

    /**
     * Render a new [state].
     * This function will be called each time a new state is received.
     *
     *  **This function must be idempotent, pure, and should not send any intents**
     *
     *  If your subscriber is stateful (such as an Android Fragment and its views), make sure to always update
     *  **all of the components (views) of the subscriber on each state change**
     */
    public fun render(state: S)
}
