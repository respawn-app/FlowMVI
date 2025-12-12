package pro.respawn.flowmvi.sample.di

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.invoke
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val LogExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    PlatformStoreLogger(StoreLogLevel.Error, "ApplicationScope") {
        "Unhandled coroutine exception: ${throwable.stackTraceToString()}".trim()
    }
}

/**
 * Application-wide coroutine scope for the sample app.
 *
 * This scope is intended to live for the entire app lifetime. It uses its own [SupervisorJob]
 * (optionally linked to a parent) and [Dispatchers.Default].
 *
 * Lifecycle contract:
 * - Created once on DI bootstrap.
 * - Must be [close]d on app shutdown (Koin `onClose` does this) to cancel all running jobs and avoid leaks.
 */
class ApplicationScope(
    parent: CoroutineScope? = null,
) : CoroutineScope, AutoCloseable {

    private val job: Job = SupervisorJob(parent?.coroutineContext?.get(Job))

    override val coroutineContext: CoroutineContext =
        (parent?.coroutineContext ?: EmptyCoroutineContext) +
            job +
            Dispatchers.Default +
            CoroutineName("ApplicationScope") +
            LogExceptionHandler

    fun runJob(
        timeout: Duration = 10.seconds,
        context: CoroutineContext = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit,
    ) = launch(context) { withTimeout(timeout, block) }

    override fun close() = job.cancel()
}
