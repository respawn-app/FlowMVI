package pro.respawn.flowmvi.api

/**
 * A [consume]r of [Store]'s events that has certain state [S].
 * Each [Consumer] needs a container, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 */
public interface Consumer<S : MVIState, I : MVIIntent, A : MVIAction> : IntentReceiver<I>, StateConsumer<S> {

    /**
     * Container, an object that wraps a Store.
     */
    public val container: Container<S, I, A>
    override fun intent(intent: I): Unit = container.store.send(intent)
    override suspend fun emit(intent: I): Unit = container.store.emit(intent)
}
