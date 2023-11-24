package pro.respawn.flowmvi.api

import androidx.compose.runtime.Stable

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * Usually not subclassed but used with a corresponding builder (see [pro.respawn.flowmvi.dsl.store]).
 * A store functions independently of any subscribers, has its own lifecycle, can be stopped and relaunched at will.
 * The store can be mutated only through [MVIIntent].
 * Store is an [IntentReceiver] and can be [close]d to stop it.
 */
@Stable
public interface Store<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    ImmutableStore<S, I, A>,
    IntentReceiver<I>
