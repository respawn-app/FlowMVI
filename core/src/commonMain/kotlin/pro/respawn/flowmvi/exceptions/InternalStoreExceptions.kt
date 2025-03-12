package pro.respawn.flowmvi.exceptions

import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.UnrecoverableException

internal class NonSuspendingSubscriberException(name: String?, cause: Exception? = null) : UnrecoverableException(
    cause = cause,
    message = """
        You have subscribed to ${name ?: "the store"}, but your subscribe() block has returned early (without throwing a
        CancellationException). When you subscribe, make sure to continue collecting values from the store until the Job 
        Returned from the subscribe() is cancelled as you likely don't want to stop being subscribed to the store
        (i.e. complete the subscription job on your own). Set allowTransientSubscriptions = true to suppress. 
    """.trimIndent(),
)

internal class UnhandledIntentException(intent: MVIIntent, cause: Exception? = null) : UnrecoverableException(
    cause = cause,
    message = """
        An intent "$intent" not been handled after calling all plugins. 
        You likely don't want this to happen because intents are supposed to be acted upon.
        Make sure you have at least one plugin that handles intents, such as reducePlugin(), or set debuggable = false
        to suppress the error.
    """.trimIndent(),
)

internal class RecursiveRecoverException(name: String?, cause: Exception) : UnrecoverableException(
    cause = cause,
    message = """
        Recursive recover detected in ${name ?: "Store"}, which means you have thrown in a recover plugin or in the `onException` block.
        Please never throw while recovering from exceptions, or that will result in an infinite loop.
    """.trimIndent()
)

internal class UnhandledStoreException(name: String?, cause: Exception) : UnrecoverableException(
    cause = cause,
    message = """
        ${name ?: "this store"} has run all its plugins (exception handlers) but the exception was not handled by any of them.
        Common solutions:
        1. Add a recover() plugin
        2. Review your existing plugins to ensure they handle this type of exception
        3. Consider adding a catch-all handler if this is an expected behavior
    """.trimIndent(),
)

internal class ActionsDisabledException(name: String?) : UnrecoverableException(
    message = """
        Actions are disabled for ${name ?: "this store"}, but you tried to consume or send one.
        This is most likely a developer error. Either enable actions or do not attempt to send them.
    """.trimIndent()
)

internal class SubscribeBeforeStartException(name: String?, cause: Exception? = null) : UnrecoverableException(
    cause = cause,
    message = """
        You have attempted to subscribe to ${name ?: "this store"} before starting it.
        If this is intended, set allowIdleSubscriptions = true for the store. 
        If not, please always call Store.start() before you try using it.
    """.trimIndent()
)

@PublishedApi
internal class RecursiveStateTransactionException(cause: Exception?) : UnrecoverableException(
    cause = cause,
    message = """
        You have tried to start a state transaction while already in one.
        This happened because state transactions are Atomic and reentrant = false.
        Please avoid using recursion in transactions, otherwise you will get a permanent deadlock, or use 
        StateStrategy.Atomic.reentrant = true at the cost of some performance.
    """.trimIndent()
)
