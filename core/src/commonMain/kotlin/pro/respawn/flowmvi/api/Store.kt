package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * Usually not subclassed but used with a corresponding builder (see [pro.respawn.flowmvi.dsl.store]).
 * A store functions independently of any subscribers, has its own lifecycle, can be stopped and relaunched at will.
 * The store can be mutated only through [MVIIntent].
 * Store is an [IntentReceiver] and can be [close]d to stop it.
 */
@OptIn(ExperimentalStdlibApi::class)
public interface Store<out S : MVIState, in I : MVIIntent, out A : MVIAction> : IntentReceiver<I>, AutoCloseable {

    /**
     *  The name of the store. Used for debugging purposes and when storing multiple stores in a collection.
     *  Optional and configured through a [pro.respawn.flowmvi.dsl.StoreBuilder]
     */
    public val name: String?

    /**
     * An initial [MVIState] this [Store] starts with. This is the value used when the store is created only and will
     * **not** be restored upon [close] ing the store or [subscribe]ing to it.
     * Mandatory and configured through [pro.respawn.flowmvi.dsl.StoreBuilder].
     */
    public val initial: S

    /**
     * Starts store intent processing in a new coroutine in the given [scope].
     * Intents are processed as long as the parent scope is active.
     * **Starting store processing when it is already started will result in an exception.**
     * Although not always needed, store can be launched multiple times,
     * assuming you cancel the job used before or call [close].
     * @return a [Job] that the store is running on that can be cancelled later. [close] will cancel that job.
     */
    public fun start(scope: CoroutineScope): Job

    /**
     * Subscribe to the store, obtaining a [Provider] to consume [MVIState]s and [MVIAction]s.
     * The store itself does not expose actions or states to prevent subscribers from affecting the store and to keep
     * track of each subscription.
     * When [subscribe] is invoked, a new [StorePlugin.onSubscribe] event is sent to all plugins **and then** the
     * subscription count is incremented.
     * For more, see [StorePlugin]
     */
    public fun CoroutineScope.subscribe(block: suspend Provider<S, I, A>.() -> Unit): Job
}
