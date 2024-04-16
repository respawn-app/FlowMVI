package pro.respawn.flowmvi.sample.util

import pro.respawn.flowmvi.sample.BuildFlags
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual val BuildFlags.debuggable get() = kotlin.native.Platform.isDebugBinary
internal actual val BuildFlags.platform: Platform get() = Platform.Apple
