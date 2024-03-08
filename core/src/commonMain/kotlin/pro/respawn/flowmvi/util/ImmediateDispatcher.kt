package pro.respawn.flowmvi.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.concurrent.Volatile

@Volatile
private var isImmediateSupported: Boolean = true

@Suppress("UnusedReceiverParameter")
public val MainCoroutineDispatcher.immediateOrDefault: MainCoroutineDispatcher
    get() {
        if (isImmediateSupported) {
            try {
                return Dispatchers.Main.immediate
            } catch (ignored: UnsupportedOperationException) {
            } catch (ignored: NotImplementedError) {
            }

            isImmediateSupported = false
        }
        return Dispatchers.Main
    }
