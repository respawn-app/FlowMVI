package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * Usually not subclassed but used with a corresponding builder (see [pro.respawn.flowmvi.dsl.store]).
 *
 * * A Store functions independently of any subscribers,
 * * Store has its own [StoreLifecycle], can be stopped and relaunched at will via [StoreLifecycle] returned
 *   from [start] or as this [StoreLifecycle] reference.
 * * The store can be mutated only through [MVIIntent].
 * * Store is an [IntentReceiver]
 */
public interface Store<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    ImmutableStore<S, I, A>,
    IntentReceiver<I>,
    StoreLifecycle {

    // mutable return type
    override fun start(scope: CoroutineScope): StoreLifecycle
}
