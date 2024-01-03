package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver

private object NoOpSaver : Saver<Nothing> {

    override suspend fun save(state: Nothing?): Unit = Unit
    override suspend fun restore(): Nothing? = null
    override suspend fun recover(e: Exception): Nothing? = null
}

/**
 * A [Saver] that will do nothing to save the state and will not [Saver.restore] to any state.
 *
 * Useful for testing.
 */
@Suppress("UNCHECKED_CAST", "FunctionName")
public fun <S> NoOpSaver(): Saver<S> = NoOpSaver as Saver<S>
