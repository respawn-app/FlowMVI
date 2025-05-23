@file:OptIn(ExperimentalFlowMVIAPI::class)

package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.context.SubscriptionAware
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.util.withPrevious

internal fun subscriptionModule(): SubscriptionModule = SubscriptionModuleImpl()

/**
 * This class is used to mark the collectors as subscribed, it's needed because
 * we can't really tell what the subscriber is collecting - states or actions, hence using
 * those flows is not sufficient.
 **/
internal interface SubscriptionModule : SubscriptionAware {

    suspend fun awaitUnsubscription()
}

private class SubscriptionModuleImpl : SubscriptionModule {

    private val marker = MutableSharedFlow<Nothing>()

    override val subscriberCount by marker::subscriptionCount

    override suspend fun awaitUnsubscription() = marker.collect()
}

internal suspend inline fun SubscriptionAware.observeSubscribers(
    crossinline onSubscribe: suspend (count: Int) -> Unit,
    crossinline onUnsubscribe: suspend (count: Int) -> Unit,
) = subscriberCount.withPrevious(0).collect { (previous, new) ->
    when {
        new > previous -> onSubscribe(new)
        new < previous -> onUnsubscribe(new)
    }
}

internal inline val PluginInstance<*, *, *>.observesSubscribers get() = onSubscribe != null || onUnsubscribe != null
