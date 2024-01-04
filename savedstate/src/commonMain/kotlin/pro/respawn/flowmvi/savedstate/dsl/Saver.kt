package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] builder function
 */
public inline fun <T> Saver(
    crossinline save: suspend (T?) -> Unit,
    crossinline restore: suspend () -> T?,
    crossinline recover: suspend (e: Exception) -> T? = { throw it },
): Saver<T> = object : Saver<T> {
    override suspend fun save(state: T?) = save.invoke(state)
    override suspend fun restore(): T? = restore.invoke()
    override suspend fun recover(e: Exception): T? = recover.invoke(e)
}
