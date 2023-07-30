package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * Usually not subclassed but used with a corresponding builder (see [lazyStore], [launchedStore]).
 * A store functions independently of any subscribers.
 * MVIStore is the base implementation of [MVIProvider].
 */
public interface Store<S : MVIState, I : MVIIntent, A : MVIAction> : IntentReceiver<I> {

    public val name: String
    public val initial: S

    /**
     * Starts store intent processing in a new coroutine in the given [scope].
     * Intents are processed as long as the parent scope is active.
     * **Starting store processing when it is already started will result in an exception.**
     * Although not advised, store can be launched multiple times, provided you cancel the job used before.
     * @return a [Job] that the store is running on that can be cancelled later.
     */
    public fun start(scope: CoroutineScope): Job

    public fun CoroutineScope.subscribe(block: suspend Provider<S, I, A>.() -> Unit): Job

}

public interface MutableStore<S : MVIState, I : MVIIntent, A : MVIAction> :
    Store<S, I, A>,
    StateReceiver<S>,
    ActionReceiver<A>
