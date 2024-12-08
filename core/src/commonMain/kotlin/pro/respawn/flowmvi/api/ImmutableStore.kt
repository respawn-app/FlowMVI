package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.lifecycle.ImmutableStoreLifecycle
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle

/**
 * A [Store] that does not allow sending intents.
 * @see Store
 */
public interface ImmutableStore<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    ImmutableStoreLifecycle,
    StateProvider<S> {

    /**
     *  The name of the store. Used for debugging purposes and when storing multiple stores in a collection.
     *  Optional and configured through a [pro.respawn.flowmvi.dsl.StoreBuilder]
     */
    public val name: String?

    /**
     * Starts store intent processing in a new coroutine in the given [scope].
     * Intents are processed as long as the parent scope is active.
     *
     * **Starting store processing when it is already started will result in an exception.**
     *
     * Although not always needed, store can be launched multiple times,
     * assuming you cancel the job used before or call [Store.close].
     *
     * Returns an [ImmutableStoreLifecycle] that the store is running on that can be cancelled later.
     * A mutable version [Store] returns a mutable [StoreLifecycle] that can be used to stop the store.
     *
     * The [Store] also implements [StoreLifecycle] but there is an **important** distinction between the one returned
     * by this method and the [Store] itself. The returned reference only tracks the lifecycle resulting from this call,
     * while the [Store] tracks **all** starts and stops as a single lifecycle.
     *
     * For example, waiting for startup or closing the returned lifecycle multiple times is a no-op, while the store
     * object itself will have the opposite behavior and wait for the next [start] call.
     */
    public fun start(scope: CoroutineScope): ImmutableStoreLifecycle

    /**
     * Subscribe to the store, obtaining a [Provider] to consume [MVIState]s and [MVIAction]s.
     * The store itself does not expose actions or states to prevent subscribers from affecting the store and to keep
     * track of each subscription.
     * When [subscribe] is invoked, a new [StorePlugin.onSubscribe] event is sent to all plugins with the
     * new subscriber count. For more, see [StorePlugin].
     */
    public fun CoroutineScope.subscribe(block: suspend Provider<S, I, A>.() -> Unit): Job

    @InternalFlowMVIAPI
    override val states: StateFlow<S>
    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}
