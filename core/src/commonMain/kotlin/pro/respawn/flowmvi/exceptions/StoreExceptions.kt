@file:Suppress("FunctionName")

package pro.respawn.flowmvi.exceptions

import pro.respawn.flowmvi.api.UnrecoverableException

internal fun NonSuspendingSubscriberException() = UnrecoverableException(
    message = """
        You have subscribed to the store, but your subscribe() block has returned early (without throwing a
        CancellationException). When you subscribe, make sure to continue collecting values from the store until the Job 
        Returned from the subscribe() is cancelled as you likely don't want to stop being subscribed to the store
        (i.e. complete the subscription job on your own). 
    """
        .trimIndent(),
    cause = null,
)

internal fun UnhandledIntentException() = UnrecoverableException(
    message = """
        An intent has not been handled after calling all plugins. 
        You likely don't want this to happen because intents are supposed to be acted upon.
        Make sure you have at least one plugin that handles intents, such as reducePlugin().
    """
        .trimIndent(),
    cause = null,
)

internal fun RecursiveRecoverException(cause: Exception) = UnrecoverableException(
    message = """
        Recursive recover detected, which means you have thrown in a recover plugin or in the `onException` block.
        Please never throw while recovering from exceptions, or that will result in an infinite loop.
    """.trimIndent(),
    cause = cause
)

internal fun UnhandledStoreException(cause: Exception) = UnrecoverableException(
    message = """
        Store has run all its plugins (exception handlers) but the exception was not handled by any of them.
    """.trimIndent(),
    cause = cause,
)

internal fun ActionsDisabledException() = UnrecoverableException(
    message = """
        Actions are disabled for this store, but you tried to consume or send one.
        This is most likely a developer error. Either enable actions or do not attempt to send them.
    """.trimIndent()
)
