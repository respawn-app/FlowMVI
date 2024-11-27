package pro.respawn.flowmvi.exceptions

import pro.respawn.flowmvi.api.UnrecoverableException

internal class NonSuspendingSubscriberException() : UnrecoverableException(
    message = """
        You have subscribed to the store, but your subscribe() block has returned early (without throwing a
        CancellationException). When you subscribe, make sure to continue collecting values from the store until the Job 
        Returned from the subscribe() is cancelled as you likely don't want to stop being subscribed to the store
        (i.e. complete the subscription job on your own). Set debuggable = false to suppress. 
    """
        .trimIndent(),
    cause = null,
)

internal class UnhandledIntentException() : UnrecoverableException(
    message = """
        An intent has not been handled after calling all plugins. 
        You likely don't want this to happen because intents are supposed to be acted upon.
        Make sure you have at least one plugin that handles intents, such as reducePlugin(), or set debuggable = false
        to suppress the error.
    """
        .trimIndent(),
    cause = null,
)

internal class RecursiveRecoverException(cause: Exception) : UnrecoverableException(
    message = """
        Recursive recover detected, which means you have thrown in a recover plugin or in the `onException` block.
        Please never throw while recovering from exceptions, or that will result in an infinite loop.
    """.trimIndent(),
    cause = cause
)

internal class UnhandledStoreException(cause: Exception) : UnrecoverableException(
    message = """
        Store has run all its plugins (exception handlers) but the exception was not handled by any of them.
    """.trimIndent(),
    cause = cause,
)

internal class ActionsDisabledException() : UnrecoverableException(
    message = """
        Actions are disabled for this store, but you tried to consume or send one.
        This is most likely a developer error. Either enable actions or do not attempt to send them.
    """.trimIndent()
)

internal class SubscribeBeforeStartException() : UnrecoverableException(
    message = """
        You have attempted to subscribe to the store before starting it.
        If this is intended, set allowIdleSubscriptions = true for the store. 
        If not, please always call Store.start() before you try using it.
    """.trimIndent()
)
