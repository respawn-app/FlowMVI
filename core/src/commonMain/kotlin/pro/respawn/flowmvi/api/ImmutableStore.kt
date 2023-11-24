package pro.respawn.flowmvi.api

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * A [Store] that does not allow sending intents.
 * @see Store
 */
@OptIn(ExperimentalStdlibApi::class)
@Stable
public interface ImmutableStore<out S : MVIState, in I : MVIIntent, out A : MVIAction> : AutoCloseable {

    /**
     *  The name of the store. Used for debugging purposes and when storing multiple stores in a collection.
     *  Optional and configured through a [pro.respawn.flowmvi.dsl.StoreBuilder]
     */
    public val name: String?

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

    /**
     * Obtain the current state in an unsafe manner.
     * This property is not thread-safe and parallel state updates will introduce a race condition when not
     * handled properly.
     * Such race conditions arise when using multiple data streams such as [Flow]s.
     *
     * Accessing the state this way will **circumvent ALL plugins**.
     */
    @DelicateStoreApi
    public val state: S
}
