package pro.respawn.flowmvi.api

/**
 * A consumer of [Store]'s events that has certain state [S].
 * Each [Consumer] needs a container, a way to [emit] intents to it,
 * a way to [render] the new state.
 */
public interface Consumer<S : MVIState, I : MVIIntent, A : MVIAction> : IntentReceiver<I>, StateConsumer<S> {

    /**
     * Container, an object that wraps a Store.
     */
    public val container: Container<S, I, A>
    override fun intent(intent: I): Unit = container.store.intent(intent)
    override suspend fun emit(intent: I): Unit = container.store.emit(intent)
}
