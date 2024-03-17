package pro.respawn.flowmvi.compose.dsl

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle
import pro.respawn.flowmvi.compose.api.SubscriptionMode

/**
 * Delegate the [repeatOnLifecycle] to another object. Prefer using this to the interface
 * to preserve the hashcode/equals contract of the parent
 */
public inline fun <T : Any> SubscriberLifecycle(
    delegate: T,
    crossinline repeatOnLifecycle: suspend T.(mode: SubscriptionMode, block: suspend CoroutineScope.() -> Unit) -> Unit,
): SubscriberLifecycle = object : SubscriberLifecycle {
    override suspend fun repeatOnLifecycle(
        mode: SubscriptionMode,
        block: suspend CoroutineScope.() -> Unit
    ) = delegate.repeatOnLifecycle(mode, block)

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
}
