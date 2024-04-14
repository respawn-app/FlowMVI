package pro.respawn.flowmvi.sample.util

import kotlin.experimental.ExperimentalNativeApi

actual object BuildFlags {

    @OptIn(ExperimentalNativeApi::class)
    actual val debuggable: Boolean = kotlin.native.Platform.isDebugBinary
    actual val platform: Platform = Platform.Apple
}
