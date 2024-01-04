package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] implementation that does nothing but invoke
 * the given [onSave], [onRestore], [onException] callbacks when the state changes
 * @see Saver
 */
public inline fun <T> CallbackSaver(
    delegate: Saver<T>,
    crossinline onSave: suspend (T?) -> Unit = {},
    crossinline onRestore: suspend (T?) -> Unit = { },
    crossinline onException: suspend (e: Exception) -> Unit = {},
): Saver<T> = object : Saver<T> by delegate {
    override suspend fun save(state: T?) {
        onSave(state)
        return delegate.save(state)
    }

    override suspend fun restore(): T? = delegate.restore().also { onRestore(it) }

    override suspend fun recover(e: Exception): T? {
        onException(e)
        return delegate.recover(e)
    }
}
