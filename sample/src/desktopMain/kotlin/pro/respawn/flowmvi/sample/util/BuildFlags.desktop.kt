package pro.respawn.flowmvi.sample.util

import pro.respawn.flowmvi.sample.BuildFlags

internal actual val BuildFlags.debuggable: Boolean get() = true
internal actual val BuildFlags.platform get() = Platform.Desktop
