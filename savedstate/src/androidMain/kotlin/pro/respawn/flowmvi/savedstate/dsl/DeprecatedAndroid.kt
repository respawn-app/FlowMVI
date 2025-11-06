@file:Suppress("FunctionName")

package pro.respawn.flowmvi.savedstate.dsl

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.platform.key

/**
 * A [Saver] implementation that saves the specified value of [T] to a [handle].
 * The type of [T] **must** be saveable in a bundle, or the framework code will throw.
 * If your state is [Parcelable], use the [ParcelableSaver] instead.
 */
@Deprecated(RecoverDeprecationMessage)
public fun <T> SavedStateHandleSaver(
    handle: SavedStateHandle,
    key: String,
    recover: suspend (e: Exception) -> T?,
): Saver<T> = object : Saver<T> {

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override suspend fun recover(e: Exception): T? = recover.invoke(e)
    override suspend fun restore(): T? = handle[key]
    override suspend fun save(state: T?) {
        if (state == null) handle.remove<T>(key) else handle[key] = state
    }
}

/**
 * A [Saver] implementation that saves the given [Parcelable] state to a [handle].
 *
 * The [key] parameter is derived from the simple class name of the state by default.
 */
@Deprecated(RecoverDeprecationMessage)
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
public inline fun <reified T> ParcelableSaver(
    handle: SavedStateHandle,
    key: String = key<T>(),
    noinline recover: suspend (e: Exception) -> T?,
): Saver<T> where T : Parcelable, T : MVIState = SavedStateHandleSaver(handle, key, recover)
