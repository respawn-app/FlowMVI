package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.util.withType

public inline fun <reified T : S, S : MVIState> TypedSaver(
    delegate: Saver<T>,
): Saver<S> = object : Saver<S> {
    override suspend fun restore(): S? = delegate.restore()
    override suspend fun recover(e: Exception): S? = delegate.recover(e)
    override suspend fun save(state: S?) {
        state.withType<T, _> { delegate.save(this) }
    }
}
