@file:MustUseReturnValue

package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * A lightweight [Saver] implementation that will do nothing to save the state and will not [Saver.restore] to any state.
 *
 * Useful for testing.
 */
private class NoOpSaverImpl<T> : Saver<T> {

    override suspend fun save(state: T?): Unit = Unit
    override suspend fun restore(): T? = null
}

/**
 * A [Saver] that will do nothing to save the state and will not [Saver.restore] to any state.
 *
 * Useful for testing.
 */
public fun <S> NoOpSaver(): Saver<S> = NoOpSaverImpl()
