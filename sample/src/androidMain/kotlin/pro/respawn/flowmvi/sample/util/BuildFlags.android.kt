package pro.respawn.flowmvi.sample.util

import pro.respawn.flowmvi.BuildConfig

actual object BuildFlags {

    actual val debuggable: Boolean = BuildConfig.DEBUG
    actual val platform = Platform.Android

}
