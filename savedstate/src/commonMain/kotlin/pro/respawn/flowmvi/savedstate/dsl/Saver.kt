@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] builder function
 */
public inline fun <T> Saver(
    crossinline save: suspend (T?) -> Unit,
    crossinline restore: suspend () -> T?,
): Saver<T> = object : Saver<T> {
    override suspend fun save(state: T?) = save.invoke(state)
    override suspend fun restore(): T? = restore.invoke()
}
