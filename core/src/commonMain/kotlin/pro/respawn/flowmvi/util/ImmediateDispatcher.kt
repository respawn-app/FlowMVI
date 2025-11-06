package pro.respawn.flowmvi.util

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

private var isImmediateSupported: Boolean by atomic(true)

/**
 * Obtain a [MainCoroutineDispatcher.immediate], and if not supported by the current platform, fall back to a
 * default [MainCoroutineDispatcher].
 */
@Suppress("UnusedReceiverParameter")
public val MainCoroutineDispatcher.immediateOrDefault: MainCoroutineDispatcher
    get() {
        if (isImmediateSupported) {
            try {
                return Dispatchers.Main.immediate
            } catch (_: UnsupportedOperationException) {
            } catch (_: NotImplementedError) {
            }

            isImmediateSupported = false
        }
        return Dispatchers.Main
    }
