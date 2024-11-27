package pro.respawn.flowmvi.exceptions

import kotlin.time.Duration

/**
 * Exception thrown when the operation in the store has timed out.
 * Unlike regular `CancellationException`, is caught by the [pro.respawn.flowmvi.api.StorePlugin.onException] handler and the recover plugin.
 */
public class StoreTimeoutException(timeout: Duration) : RuntimeException(
    message = "Store has timed out after $timeout."
)
