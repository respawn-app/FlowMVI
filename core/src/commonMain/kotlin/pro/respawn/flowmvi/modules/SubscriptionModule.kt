package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.util.withPrevious

internal fun subscriptionModule(): SubscriptionModule = SubscriptionModuleImpl()

/**
 * This class is used to mark the collectors as subscribed, it's needed because
 * we can't really tell what the subscriber is collecting - states or actions, hence using
 * those flows is not sufficient.
 **/
internal interface SubscriptionModule {

    val subscribers: Flow<Pair<Int, Int>>

    suspend fun awaitUnsubscription(): Nothing
}

private class SubscriptionModuleImpl : SubscriptionModule {

    private val marker = MutableSharedFlow<Nothing>()
    override val subscribers = marker
        .subscriptionCount
        .withPrevious(0)

    override suspend fun awaitUnsubscription() = marker.collect { }
}

internal suspend inline fun SubscriptionModule.observeSubscribers(
    crossinline onSubscribe: suspend (count: Int) -> Unit,
    crossinline onUnsubscribe: suspend (count: Int) -> Unit,
) = subscribers.collect { (previous, new) ->
    when {
        new > previous -> onSubscribe(new)
        new < previous -> onUnsubscribe(new)
    }
}

internal inline val PluginInstance<*, *, *>.observesSubscribers get() = onSubscribe != null || onUnsubscribe != null
