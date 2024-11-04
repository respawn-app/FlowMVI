package pro.respawn.flowmvi.api.lifecycle

import pro.respawn.flowmvi.api.Store

public interface ImmutableStoreLifecycle {

    /**
     * Wait while the [Store] is started. If it is already started, returns immediately.
     */
    public suspend fun awaitStartup()

    /**
     * Suspend until the store is closed
     */
    public suspend fun awaitUntilClosed()

    /**
     * Whether the [Store] is active (store is started or **being started**).
     */
    public val isActive: Boolean

    /**
     * Whether the [Store] has started fully.
     *
     * Unlike [isActive], returns `false` until store is fully started.
     */
    public val isStarted: Boolean
}
