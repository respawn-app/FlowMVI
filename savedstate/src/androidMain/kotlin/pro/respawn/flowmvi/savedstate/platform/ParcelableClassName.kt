package pro.respawn.flowmvi.savedstate.platform

import android.os.Parcelable

@PublishedApi
internal inline fun <reified T : Parcelable> key(): String = requireNotNull(T::class.qualifiedName) {
    "Cannot use anonymous class as a key for saving state"
} 
