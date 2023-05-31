package pro.respawn.flowmvi

import pro.respawn.flowmvi.store.Store
import kotlin.jvm.JvmName

/**
 * A [consume]r of [MVIProvider]'s events that has certain state [S].
 * Each [MVIView] needs a store, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 * @see MVIProvider
 * @See MVISubscriber
 */
public interface MVIView<S : MVIState, in I : MVIIntent, A : MVIAction> : MVISubscriber<S, A> {

    /**
     * Provider, an object that handles business logic.
     * @See MVIProvider
     */
    public val store: Store<S, I>

    /**
     * Send an intent for the [store] to process e.g. a user click.
     */
    public fun send(intent: I): Unit = store.send(intent)

    /**
     * @see MVIProvider.send
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    public fun I.send(): Unit = send(this)
}

/**
 * A generic subscriber of [MVIProvider] that [consume]s [MVIAction]s and [render]s [MVIState]s of types [A] and [S].
 * For a more fully defined version, see [MVIView].
 */
public interface MVISubscriber<in S : MVIState, in A : MVIAction> {

    /**
     * Render a new [state].
     * This function will be called each time a new state is received.
     *
     * This function should be idempotent, pure, and should not send any intents.
     */
    public fun render(state: S)

    /**
     * Consume a one-time side-effect emitted by [MVIProvider].
     *
     * This function is called each time an [MVIAction] arrives.
     * This function may send intents under the promise that no loops will occur.
     */
    public fun consume(action: A)
}
