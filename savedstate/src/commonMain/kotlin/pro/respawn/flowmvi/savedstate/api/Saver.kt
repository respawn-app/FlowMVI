package pro.respawn.flowmvi.savedstate.api

import pro.respawn.flowmvi.savedstate.dsl.RecoverDeprecationMessage
import pro.respawn.flowmvi.savedstate.plugins.saveStatePlugin

/**
 * A Saver is an object that specifies **how** to save the state of the [saveStatePlugin].
 * See docs for specific functions to learn how to override them.
 */
public interface Saver<T> {

    /**
     * [save] function should persist the state to some place that outlives the lifespan of the store
     * If `null` is passed to [save], it **must** clean up the persisted state completely.
     * You must adhere to this contract if you implement your own saver.
     */
    public suspend fun save(state: T?)

    /**
     * [restore] function should restore the state from the storage.
     * It could be a file, a bundle on Android, or some other place. State is restored **before** the store starts,
     * so it is not advised to suspend in this function for very long periods of time.
     * If this function returns `null`, the state will not be restored (there is nothing to restore).
     */
    public suspend fun restore(): T?

    /** [recover] allows to return a state to save and restore even if an exception has been
     *  caught while the saver was working. By default, it just throws the exception and the parent store will decide
     *  how to handle it.
     **/
    @Deprecated(RecoverDeprecationMessage)
    public suspend fun recover(e: Exception): T? = throw e
}
