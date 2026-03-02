@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.savedstate.dsl

import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.util.typed

/**
 * A [Saver] that maps the saved state to a value of [T] before passing it to the [delegate].
 *
 * It will not map `null` values.
 */
@Suppress("DEPRECATION") // TODO: remove recover
public inline fun <reified T, R> MapSaver(
    delegate: Saver<R>,
    @BuilderInference crossinline from: suspend (R) -> T?,
    @BuilderInference crossinline to: suspend (T?) -> R?,
): Saver<T> = Saver(
    save = { delegate.save(to(it)) },
    restore = { delegate.restore()?.let { from(it) } },
    recover = { e -> delegate.recover(e)?.let { from(it) } },
)

/**
 * A [MapSaver] that will only persist values of type [T].
 *
 * @see Saver
 */
public inline fun <reified T : S, reified S> TypedSaver(
    delegate: Saver<T>,
): Saver<S> = MapSaver(
    delegate = delegate,
    from = { it },
    to = { it.typed<T>() },
)
