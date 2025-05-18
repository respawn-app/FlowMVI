package pro.respawn.flowmvi.exceptions

// todo: unrecoverable?
public class StoreAlreadyStartedException internal constructor(name: String?) : IllegalStateException(
    """
        The store ${name.orEmpty()} is already started.
        You can restart a store, but ensure you try to start it only when it is not already active.
        To check if a store is active, use the `StoreLifecycle.isActive` property.
    """.trimIndent()
)
