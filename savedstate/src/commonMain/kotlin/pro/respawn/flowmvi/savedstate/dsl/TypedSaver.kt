package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.util.typed

public inline fun <reified T, R> MapSaver(
    delegate: Saver<R>,
    @BuilderInference crossinline from: suspend (R?) -> T?,
    @BuilderInference crossinline to: suspend (T?) -> R?,
): Saver<T> = object : Saver<T> {
    override suspend fun save(state: T?) = delegate.save(to(state))
    override suspend fun restore(): T? = from(delegate.restore())
    override suspend fun recover(e: Exception): T? = from(delegate.recover(e))
}

public inline fun <reified T : S, reified S> TypedSaver(
    delegate: Saver<T>,
): Saver<S> = MapSaver(
    delegate = delegate,
    from = { it },
    to = { it.typed<T>() },
)
