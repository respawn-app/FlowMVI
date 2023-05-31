// false-positive https://youtrack.jetbrains.com/issue/KTIJ-7642
@file:Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")

package pro.respawn.flowmvi.base

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

/**
 * An interface for defining a function that will [reduce] incoming [MVIIntent]s.
 * Similar to [Reduce], but can be implemented somewhere else and composed.
 * For ways to convert this to [Reduce] to create a store, see extension functions `recover` and `reducer`.
 */
public fun interface Reducer<S : MVIState, I : MVIIntent> {

    /**
     * Reduce consumer's intent to a new [MVIState] or zero or more [MVIAction]s.
     * Use [MVIStore.send] for sending side-effects for the [MVISubscriber] to handle.
     * Coroutines launched inside [reduce] can fail independently of each other.
     */
    public suspend operator fun SuspendPipelineContext<S, I>.invoke(intent: I)
}
