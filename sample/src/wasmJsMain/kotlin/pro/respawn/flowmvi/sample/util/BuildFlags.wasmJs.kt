package pro.respawn.flowmvi.sample.util

import pro.respawn.flowmvi.BuildFlags

// TODO: Try to properly resolve?
internal actual val BuildFlags.debuggable: Boolean get() = true
internal actual val BuildFlags.platform: Platform get() = Platform.Web
