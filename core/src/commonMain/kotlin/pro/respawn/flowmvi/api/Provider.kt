package pro.respawn.flowmvi.api

/**
 * An entity that handles [MVIIntent]s, produces [actions] and manages [states].
 * Provider is available when you call [Store.subscribe] and allows you to consume your [states] and [actions]
 * Provider is a:
 * * [StateProvider]
 * * [IntentReceiver]
 * * [ActionProvider]
 */
public interface Provider<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    StateProvider<S>,
    IntentReceiver<I>,
    ActionProvider<A>
