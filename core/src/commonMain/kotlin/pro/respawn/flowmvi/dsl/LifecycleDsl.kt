package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.api.SubscriberLifecycle
import pro.respawn.flowmvi.api.SubscriptionMode

/**
 * A no-op [SubscriberLifecycle] implementation that does not follow the system lifecycle in any way and ignores
 * [SubscriptionMode]
 */
public val ImmediateLifecycle: SubscriberLifecycle = SubscriberLifecycle(Unit) { _, block -> coroutineScope(block) }

/**
 * Create a [SubscriberLifecycle] by delegating the [repeatOnLifecycle] method to [delegate]
 */
public fun <T> SubscriberLifecycle(
    delegate: T,
    repeatOnLifecycle: suspend T.(mode: SubscriptionMode, block: suspend CoroutineScope.() -> Unit) -> Unit,
): SubscriberLifecycle = object : LifecycleDelegate<T>(delegate) {
    override suspend fun T.repeatOnLifecycle(
        mode: SubscriptionMode,
        block: suspend CoroutineScope.() -> Unit
    ) = repeatOnLifecycle.invoke(delegate, mode, block)
}

private abstract class LifecycleDelegate<T>(
    private val delegate: T,
) : SubscriberLifecycle {

    abstract suspend fun T.repeatOnLifecycle(
        mode: SubscriptionMode,
        block: suspend CoroutineScope.() -> Unit
    )

    override suspend fun repeatOnLifecycle(
        mode: SubscriptionMode,
        block: suspend CoroutineScope.() -> Unit
    ) = delegate.repeatOnLifecycle(mode, block)

    override fun hashCode(): Int = 42 * delegate.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        other as LifecycleDelegate<*>
        return delegate == other.delegate
    }
}
