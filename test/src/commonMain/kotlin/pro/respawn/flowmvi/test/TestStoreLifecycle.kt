package pro.respawn.flowmvi.test

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import pro.respawn.flowmvi.api.ExperimentalStoreApi
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle

@ExperimentalStoreApi
public class TestStoreLifecycle(parent: Job?) : StoreLifecycle {

    private val closed = CompletableDeferred<Unit>(parent)
    override val isActive: Boolean get() = closed.isActive
    override val isStarted: Boolean get() = closed.isActive

    override suspend fun awaitStartup(): Unit = Unit
    override suspend fun awaitUntilClosed() {
        closed.await()
    }

    override fun close() {
        closed.complete(Unit)
    }

    override suspend fun closeAndWait() {
        closed.complete(Unit)
        closed.await()
    }
}
