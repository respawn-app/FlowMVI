package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import pro.respawn.flowmvi.util.withPrevious

internal fun subscribersModule(): SubscribersModule = SubscribersModuleImpl()

/**
 * This class is used to mark the collectors as subscribed, it's needed because
 * we can't really tell what the subscriber is collecting - states or actions, hence using
 * those flows is not sufficient.
 **/
internal interface SubscribersModule {

    val subscribers: Flow<Pair<Int, Int>>

    suspend fun awaitUnsubscription(): Nothing
}

private class SubscribersModuleImpl : SubscribersModule {

    private val marker = MutableSharedFlow<Nothing>()
    override val subscribers = marker
        .subscriptionCount
        .withPrevious(0)

    override suspend fun awaitUnsubscription() = marker.collect { }
}

internal suspend inline fun SubscribersModule.observeSubscribers(
    crossinline onSubscribe: suspend (count: Int) -> Unit,
    crossinline onUnsubscribe: suspend (count: Int) -> Unit,
) = subscribers.collect { (previous, new) ->
    when {
        // although the implementation has changed, maintain previous behavior by passing previous sub value
        // to the plugins
        new > previous -> onSubscribe(new - 1)
        new < previous -> onUnsubscribe(new)
    }
}
