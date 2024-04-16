package pro.respawn.flowmvi.sample.util

import pro.respawn.flowmvi.sample.BuildConfig
import pro.respawn.flowmvi.sample.BuildFlags

internal actual val BuildFlags.debuggable: Boolean get() = BuildConfig.DEBUG
internal actual val BuildFlags.platform get() = Platform.Android
