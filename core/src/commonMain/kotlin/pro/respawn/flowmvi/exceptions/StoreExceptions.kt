package pro.respawn.flowmvi.exceptions

import pro.respawn.flowmvi.dsl.withStateOrThrow
import kotlin.time.Duration

/**
 * Exception thrown when the operation in the store has timed out.
 * Unlike regular `CancellationException`, is caught by the [pro.respawn.flowmvi.api.StorePlugin.onException] handler and the recover plugin.
 */
public class StoreTimeoutException(timeout: Duration) : RuntimeException(
    message = "Store has timed out after $timeout."
)

/**
 * Exception thrown when the state is not of desired type when using state methods that validate it, such as
 * [withStateOrThrow]
 */
public class InvalidStateException(expected: String?, got: String?) : IllegalStateException(
    message = "Expected state of type $expected but got $got"
)
