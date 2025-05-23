package pro.respawn.flowmvi.exceptions

/**
 * Exception thrown when attempting to start a store that is already active.
 *
 * This exception is thrown when [pro.respawn.flowmvi.api.Store.start] is called on a store
 * that is already in an active state. To avoid this exception, check the store's active state
 * using [pro.respawn.flowmvi.api.lifecycle.StoreLifecycle.isActive] before starting it.
 *
 * @param name The name of the store that was already started
 */
public class StoreAlreadyStartedException internal constructor(name: String?) : IllegalStateException(
    """
        The store ${name.orEmpty()} is already started.
        You can restart a store, but ensure you try to start it only when it is not already active.
        To check if a store is active, use the `StoreLifecycle.isActive` property.
    """.trimIndent()
)
