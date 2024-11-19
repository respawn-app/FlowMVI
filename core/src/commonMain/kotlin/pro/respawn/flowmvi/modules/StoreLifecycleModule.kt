package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle

internal interface StoreLifecycleModule : StoreLifecycle {

    fun completeStartup(): Boolean
}

internal interface RestartableLifecycle : StoreLifecycle {

    fun beginStartup(lifecycle: StoreLifecycle)
}

internal fun storeLifecycle(parent: Job) = object : StoreLifecycleModule {
    private val marker = CompletableDeferred<Unit>(parent)
    override val isActive get() = parent.isActive
    override val isStarted get() = isActive && marker.isCompleted

    override fun completeStartup() = marker.complete(Unit)
    override suspend fun awaitStartup() = marker.await()
    override suspend fun awaitUntilClosed() = parent.join()
    override suspend fun closeAndWait() = parent.cancelAndJoin()
    override fun close() = parent.cancel()
}

internal fun restartableLifecycle() = object : RestartableLifecycle {
    private val delegate = MutableStateFlow<StoreLifecycle?>(null)
    override val isActive: Boolean get() = delegate.value?.isActive == true
    override val isStarted: Boolean get() = delegate.value?.isStarted == true

    override suspend fun closeAndWait() = delegate.filterNotNull().first().closeAndWait()
    override suspend fun awaitStartup() = delegate.filterNotNull().first().awaitStartup()
    override suspend fun awaitUntilClosed() = delegate.filterNotNull().first().awaitUntilClosed()

    override fun beginStartup(lifecycle: StoreLifecycle) = delegate.update {
        check(it == null) { "Store is already started" }
        lifecycle
    }

    override fun close() = delegate.update {
        it?.close()
        null
    }
}