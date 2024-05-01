package pro.respawn.flowmvi.api

/**
 * An exception that has happened in the [Store] that cannot be recovered from.
 * This is either an exception resulting from developer errors (such as unhandled intents),
 * or an exception while trying to recover from another exception (which is prohibited).
 * You may also use this to bypass store plugins handling this particular exception.
 */
public open class UnrecoverableException(
    override val cause: Exception? = null,
    override val message: String? = null,
) : IllegalStateException(message, cause)
