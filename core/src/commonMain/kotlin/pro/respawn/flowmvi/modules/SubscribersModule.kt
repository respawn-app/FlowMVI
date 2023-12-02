package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal fun subscribersModule(): SubscribersModule = SubscribersModuleImpl()

internal interface SubscribersModule {

    val subscribers: StateFlow<Pair<Int, Int>>
    fun newSubscriber()
    fun removeSubscriber()
}

private class SubscribersModuleImpl : SubscribersModule {

    private val _subscribers = MutableStateFlow(0 to 0) // previous, current
    override val subscribers = _subscribers.asStateFlow()

    override fun newSubscriber() = _subscribers.update { (_, current) -> current to (current + 1).coerceAtLeast(0) }

    override fun removeSubscriber() = _subscribers.update { (_, current) -> current to (current - 1).coerceAtLeast(0) }
}

internal suspend inline fun SubscribersModule.observeSubscribers(
    crossinline onSubscribe: suspend (count: Int) -> Unit,
    crossinline onUnsubscribe: suspend (count: Int) -> Unit,
) {
    subscribers.collect { (previous, new) ->
        when {
            new > previous -> onSubscribe(new)
            new < previous -> onUnsubscribe(new)
        }
    }
}
