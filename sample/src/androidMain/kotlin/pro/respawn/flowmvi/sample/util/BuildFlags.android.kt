package pro.respawn.flowmvi.sample.util

import pro.respawn.flowmvi.BuildConfig
import pro.respawn.flowmvi.BuildFlags

internal actual val BuildFlags.debuggable: Boolean get() = BuildConfig.DEBUG
internal actual val BuildFlags.platform get() = Platform.Android
