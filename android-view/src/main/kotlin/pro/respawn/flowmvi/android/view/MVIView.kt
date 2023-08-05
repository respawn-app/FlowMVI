package pro.respawn.flowmvi.android.view

import pro.respawn.flowmvi.api.ActionConsumer
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateConsumer
import pro.respawn.flowmvi.api.Store

/**
 * A [consume]r of [Store]'s events that has certain state [S].
 * Each [MVIView] needs a container, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 */
public interface MVIView<S : MVIState, I : MVIIntent, A : MVIAction> : StateConsumer<S>, ActionConsumer<A> {

    /**
     * Container, an object that wraps a Store.
     */
    public val container: Container<S, I, A>

    /**
     * Send an intent for the [container] to process e.g. a user click.
     */
    public fun send(intent: I): Unit = container.store.send(intent)

    /**
     * @see Store.send
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    public fun I.send(): Unit = send(this)
}
