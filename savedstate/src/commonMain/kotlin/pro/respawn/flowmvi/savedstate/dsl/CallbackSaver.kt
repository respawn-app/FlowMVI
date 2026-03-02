@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A [Saver] implementation that adds additional behavior to the given [delegate]'s
 * [Saver.save] and [Saver.restore] methods.
 *
 * @see Saver
 */
public fun <T> CallbackSaver(
    delegate: Saver<T>,
    onSave: suspend (T?) -> Unit = {},
    onRestore: suspend (T?) -> Unit = {},
): Saver<T> = object : Saver<T> by delegate {
    override suspend fun save(state: T?) = onSave(state).also { delegate.save(state) }
    override suspend fun restore(): T? = delegate.restore().also { onRestore(it) }
}
