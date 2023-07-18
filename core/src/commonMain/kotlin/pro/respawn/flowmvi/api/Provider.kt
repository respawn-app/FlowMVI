package pro.respawn.flowmvi.api

/**
 * An entity that handles [MVIIntent]s, produces [actions] and manages [states].
 * This is usually the business logic unit.
 */
public interface Provider<out S : MVIState, in I : MVIIntent, A : MVIAction> :
    StateProvider<S>,
    IntentReceiver<I>,
    ActionProvider<A>
