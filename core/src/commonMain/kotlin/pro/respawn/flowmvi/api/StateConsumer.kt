package pro.respawn.flowmvi.api

public fun interface StateConsumer<in S : MVIState> {

    /**
     * Render a new [state].
     * This function will be called each time a new state is received.
     *
     * This function should be idempotent, pure, and should not send any intents.
     */
    public fun render(state: S)
}
