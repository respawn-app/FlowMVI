package pro.respawn.flowmvi.api

import pro.respawn.flowmvi.dsl.updateStateImmediate

/**
 * Defines available strategies [Store] can use when a state
 * operation ([StateReceiver.updateState] or [StateReceiver.withState])
 * is requested.
 *
 * Set during store configuration.
 */
public sealed interface StateStrategy {

    /**
     * State transactions are not [Atomic] (not serializable). This means `
     * [StateReceiver.updateState] and [StateReceiver.withState] functions are
     * no-op and forward to [updateStateImmediate].
     *
     * This leads to the following consequences:
     * 1. The order of state operations is **undefined** in parallel contexts.
     * 2. There is **no thread-safety** for state reads and writes.
     * 3. State operation **performance is increased** significantly (about 10x faster)
     *
     * * Be very careful with this strategy and use it when you will ensure the safety of updates manually **and** you
     * absolutely must squeeze the maximum performance out of a Store. Do not optimize prematurely.
     * * For a semi-safe but faster alternative, consider using [Atomic] with [Atomic.reentrant] set to `false`.
     * * This strategy configures state transactions for the whole Store.
     *   For one-time usage of non-atomic updates, see [updateStateImmediate].
     */
    public object Immediate : StateStrategy

    /**
     * Enables transaction serialization for state updates, making state updates atomic and suspendable.
     *
     * * Synchronizes state updates, allowing only **one** client to read and/or update the state at a time.
     *   All other clients attempting to get the state will wait on a FIFO queue and suspend the parent coroutine.
     * * This strategy configures state transactions for the whole store.
     *   For one-time usage of non-atomic updates, see [updateStateImmediate].
     * * Has a small performance impact because of coroutine context switching and mutex usage.
     *
     * * Performance impact can be minimized at the cost of lock reentrancy. Set [reentrant] to `false` to use it, but
     *   **HERE BE DRAGONS** if you do that, as using the state within another state transaction will
     *   cause a **permanent deadlock**.
     */
    public data class Atomic(
        val reentrant: Boolean = true,
    ) : StateStrategy
}
