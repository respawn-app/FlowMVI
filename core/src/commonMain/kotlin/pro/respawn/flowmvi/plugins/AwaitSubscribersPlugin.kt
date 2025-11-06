@file:MustUseReturnValue

package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import kotlin.time.Duration

/**
 * A class that manages the job that waits for subscribers to appear.
 * This job is set by the [awaitSubscribersPlugin] when the store is launched.
 * Use [await] or [complete] to manage the job.
 * You can only complete the job once per one start of the store.
 */
public class SubscriberManager {

    internal companion object {

        internal const val Name = "AwaitSubscribersPlugin"
    }

    private val subscriber = atomic<Job?>(null)

    /**
     * Start waiting for subscribers, suspending until the given number of subscribers arrive.
     * After the subscribers arrived, this call will return immediately
     */
    @IgnorableReturnValue
    public suspend fun await() {
        subscriber.value?.join()
    }

    /**
     * Complete the wait period, freeing the store and coroutines that called [await] to continue.
     */
    @IgnorableReturnValue
    public fun complete() {
        subscriber.getAndSet(null)?.cancel()
    }

    /**
     * Same as [complete], but suspends until completion
     */
    @IgnorableReturnValue
    public suspend fun completeAndWait(): Unit? = subscriber.getAndSet(null)?.cancelAndJoin()

    /**
     * Starts waiting for the subscribers until either [this] [CoroutineScope] is cancelled or [complete] is called.
     * Usually not called manually but rather launched by the [awaitSubscribersPlugin].
     */
    @IgnorableReturnValue
    private fun CoroutineScope.launch(timeout: Duration) {
        val previous = subscriber.value
        subscriber.getAndSet(
            launch {
                previous?.cancelAndJoin()
                withTimeoutOrNull<Nothing>(timeout) { awaitCancellation() }
            }.apply {
                invokeOnCompletion { complete() }
            }
        )
    }

    internal fun <S : MVIState, I : MVIIntent, A : MVIAction> asPlugin(
        name: String,
        timeout: Duration,
        suspendStore: Boolean,
        minSubs: Int,
    ) = plugin<S, I, A> {
        this.name = name

        onStart { launch(timeout) }

        onState { _, new ->
            if (suspendStore) await()
            new
        }
        onAction {
            if (suspendStore) await()
            it
        }
        onIntent {
            if (suspendStore) await()
            it
        }
        onStop { complete() }
        onSubscribe { current -> if (current >= minSubs) completeAndWait() }
    }
}

/**
 * Installs a new [awaitSubscribersPlugin]
 * @see awaitSubscribersPlugin
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.awaitSubscribers(
    manager: SubscriberManager = SubscriberManager(),
    minSubs: Int = 1,
    suspendStore: Boolean = true,
    timeout: Duration = Duration.INFINITE,
    name: String = SubscriberManager.Name,
): SubscriberManager = manager.also { install(awaitSubscribersPlugin(it, minSubs, suspendStore, timeout, name)) }

/**
 * Installs a new plugin using [manager] that will start waiting for new subscribers when the store launches.
 * The plugin will wait for [minSubs] subscribers for a maximum duration of [timeout].
 * If [suspendStore] is true, then the store will not process any [MVIIntent]s while it waits for subscribers.
 * This plugin starts waiting **after** plugins installed before have finished their [StorePlugin.onStart] block.
 * By default, cannot be installed multiple times, but [name] can be overridden to allow that,
 * provided that you do not reuse the [manager].
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> awaitSubscribersPlugin(
    manager: SubscriberManager,
    minSubs: Int = 1,
    suspendStore: Boolean = true,
    timeout: Duration = Duration.INFINITE,
    name: String = SubscriberManager.Name,
): StorePlugin<S, I, A> = manager.asPlugin(name, timeout, suspendStore, minSubs)
