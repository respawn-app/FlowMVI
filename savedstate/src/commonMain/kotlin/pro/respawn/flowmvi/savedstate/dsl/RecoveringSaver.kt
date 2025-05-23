package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.CancellationException
import pro.respawn.flowmvi.savedstate.api.Saver

/**
 * [Saver] that also catches exceptions during [delegate]'s [Saver.save] and [Saver.restore]
 * and handles them using [recover].
 *
 * Return `null` from [recover] to not restore / save the state, or a fallback value.
 */
public fun <T> RecoveringSaver(
    delegate: Saver<T>,
    recover: suspend (Exception) -> T?,
): Saver<T> = Saver(
    save = { delegate.saveCatching(it, recover) },
    restore = { delegate.restoreCatching(recover) },
)

@PublishedApi
internal suspend fun <S> Saver<S>.saveCatching(state: S?, recover: suspend (Exception) -> S?): Unit = try {
    save(state)
} catch (e: CancellationException) {
    throw e
} catch (expected: Exception) {
    recover.invoke(expected) ?: this.recover(expected)
    Unit
}

@PublishedApi
internal suspend fun <S> Saver<S>.restoreCatching(recover: suspend (Exception) -> S?): S? = try {
    restore()
} catch (e: CancellationException) {
    throw e
} catch (expected: Exception) {
    recover.invoke(expected) ?: this.recover(expected)
}
