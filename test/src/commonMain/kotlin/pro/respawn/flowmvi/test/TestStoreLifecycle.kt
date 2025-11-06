package pro.respawn.flowmvi.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle
import kotlin.coroutines.CoroutineContext

public class TestStoreLifecycle(parent: CoroutineScope) : StoreLifecycle, CoroutineScope {

    private val lifecycleJob = SupervisorJob(parent.coroutineContext[Job])
    override val isActive: Boolean get() = lifecycleJob.isActive
    override val isStarted: Boolean get() = lifecycleJob.isActive
    override val coroutineContext: CoroutineContext = parent.coroutineContext + lifecycleJob
    override suspend fun awaitStartup(): Unit = Unit
    override suspend fun awaitUntilClosed(): Unit = lifecycleJob.join()
    override fun close(): Unit = lifecycleJob.cancel()
    override suspend fun closeAndWait(): Unit = lifecycleJob.cancelAndJoin()
}
