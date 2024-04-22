package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
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

    private val marker = MutableSharedFlow<Unit>()
    override val subscribers = marker
        .subscriptionCount
        .withPrevious(0)
        .drop(1)

    override suspend fun awaitUnsubscription() = marker.collect { }
}

internal suspend inline fun SubscribersModule.observeSubscribers(
    crossinline onSubscribe: suspend (count: Int) -> Unit,
    crossinline onUnsubscribe: suspend (count: Int) -> Unit,
) = subscribers.collect { (previous, new) ->
    when {
        new > previous -> onSubscribe(new)
        else -> onUnsubscribe(new)
    }
}
