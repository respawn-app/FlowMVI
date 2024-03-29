package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.compose.android.asSubscriberLifecycle
import pro.respawn.flowmvi.compose.android.asSubscriptionMode
import pro.respawn.flowmvi.dsl.subscribe

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Provided [consume] parameter will be used to consume actions that come from the store.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *
 * @param lifecycleState the minimum lifecycle state that should be reached in order to subscribe to the store,
 *   upon leaving that state, the function will unsubscribe.
 * @param consume a lambda to consume actions with.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@Suppress("ComposableParametersOrdering")
@Composable
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> = subscribe(
    lifecycle = rememberSubscriberLifecycle(lifecycleOwner.lifecycle) { asSubscriberLifecycle },
    mode = lifecycleState.asSubscriptionMode,
    consume = consume
)

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will not collect [MVIAction]s.
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 * @param lifecycleState the minimum lifecycle state that should be reached in order to subscribe to the store,
 *   upon leaving that state, the function will unsubscribe.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@Composable
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): State<S> = subscribe(
    lifecycle = rememberSubscriberLifecycle(lifecycleOwner.lifecycle) { asSubscriberLifecycle },
    mode = lifecycleState.asSubscriptionMode
)
