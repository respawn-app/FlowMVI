package pro.respawn.flowmvi.api.lifecycle

import pro.respawn.flowmvi.api.Store

/**
 * [ImmutableStoreLifecycle] that is also [AutoCloseable], which can be closed on demand.
 */
public interface StoreLifecycle : ImmutableStoreLifecycle, AutoCloseable {

    /**
     * Await while the [Store] is fully closed.
     */
    public suspend fun closeAndWait()
}
