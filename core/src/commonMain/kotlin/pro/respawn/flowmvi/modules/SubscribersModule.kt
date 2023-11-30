package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal fun subscribersModule(): SubscribersModule = SubscribersModuleImpl()

internal interface SubscribersModule {

    fun CoroutineScope.observeSubscribers(
        onSubscribe: suspend (count: Int) -> Unit,
        onUnsubscribe: suspend (count: Int) -> Unit,
    )

    fun newSubscriber()
    fun removeSubscriber()
}

private class SubscribersModuleImpl : SubscribersModule {

    private var subscribers = MutableStateFlow(0 to 0) // previous, current
    override fun CoroutineScope.observeSubscribers(
        onSubscribe: suspend (count: Int) -> Unit,
        onUnsubscribe: suspend (count: Int) -> Unit
    ) {
        subscribers.onEach { (previous, new) ->
            when {
                new > previous -> onSubscribe(new)
                new < previous -> onUnsubscribe(new)
            }
        }.launchIn(this)
    }

    override fun newSubscriber() = subscribers.update { (_, current) -> current to (current + 1).coerceAtLeast(0) }

    override fun removeSubscriber() = subscribers.update { (_, current) -> current to (current - 1).coerceAtLeast(0) }
}
