@file:MustUseReturnValue

package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.CancellationException
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.UnrecoveredException

/**
 * [Saver] that also catches exceptions during [delegate]'s [Saver.save] and [Saver.restore]
 * and handles them using [recover].
 *
 * This will fallback to the deprecated [Saver.recover] if [recover] returns `null`. Library-provided savers do not use
 * the deprecated method.
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
@IgnorableReturnValue
internal suspend fun <S> Saver<S>.saveCatching(state: S?, recover: suspend (Exception) -> S?): Unit = try {
    save(state)
} catch (e: CancellationException) {
    throw e
} catch (expected: Exception) {
    val recoveryResult = recover.invoke(expected)
    // If recovery returns null, try deprecated recover but only catch the default implementation's exception
    if (recoveryResult == null) try {
        @Suppress("DEPRECATION")
        this.recover(expected)
    } catch (_: UnrecoveredException) {
        // Ignore - this means the deprecated recover is using the default implementation
    }
    Unit
}

@PublishedApi
internal suspend fun <S> Saver<S>.restoreCatching(recover: suspend (Exception) -> S?): S? = try {
    restore()
} catch (e: CancellationException) {
    throw e
} catch (expected: Exception) {
    val recoveryResult = recover.invoke(expected)
    // If recovery returns null, try deprecated recover but only catch the default implementation's exception
    recoveryResult ?: try {
        @Suppress("DEPRECATION")
        this.recover(expected)
    } catch (_: UnrecoveredException) {
        // Ignore - this means the deprecated recover is using the default implementation
        null
    }
}
