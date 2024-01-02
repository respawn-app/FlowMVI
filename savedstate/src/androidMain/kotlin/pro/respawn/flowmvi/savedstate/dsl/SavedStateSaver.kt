package pro.respawn.flowmvi.savedstate.dsl

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.ThrowRecover

public fun <T> SavedStateSaver(
    handle: SavedStateHandle,
    key: String,
    recover: suspend (e: Exception) -> T? = ThrowRecover,
): Saver<T> = object : Saver<T> {
    override suspend fun recover(e: Exception): T? = recover.invoke(e)
    override suspend fun restore(): T? = handle[key]
    override suspend fun save(state: T?) {
        if (state == null) handle.remove<T>(key) else handle[key] = state
    }
}

public fun <T : Parcelable> ParcelableSaver(
    handle: SavedStateHandle,
    key: String,
    recover: suspend (e: Exception) -> T? = ThrowRecover,
): Saver<T> = SavedStateSaver(handle, key, recover)
