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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val LogExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    PlatformStoreLogger(StoreLogLevel.Error, "ApplicationScope") {
        "Unhandled coroutine exception: ${throwable.stackTraceToString()}".trim()
    }
}

class ApplicationScope(
    parent: CoroutineScope,
) : CoroutineScope by CoroutineScope(
    parent.coroutineContext +
        SupervisorJob(parent.coroutineContext[Job]) +
        Dispatchers.Default +
        CoroutineName("ApplicationScope") +
        LogExceptionHandler
) {

    fun runJob(
        timeout: Duration = 10.seconds,
        context: CoroutineContext = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit,
    ) = launch(context) { withTimeout(timeout, block) }
}
