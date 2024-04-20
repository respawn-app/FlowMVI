package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.dsl.subscribe

private const val Message = """
An overload without an explicit Lifecycle parameter is deprecated as it is error prone in environments where
custom lifecycle is provided by the system or a navigation library. Please pass one of: 
- requireLifecycle() if you provide a lifecycle via a composition local
- DefaultLifecycle if you wish to use a platform lifecycle if no composition local is provided
- A custom component that implements Lifecycle owner if you are using an integration library
"""

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Provided [consume] parameter will be used to consume actions that come from the store.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *
 * @param mode the subscription mode that should be reached in order to subscribe to the store. At specified moments
 * in the UI lifecycle (Activity, Composable, Window etc), the store will subscribe and unsubscribe from the store.
 * @param consume a lambda to consume actions with.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@Deprecated(Message, ReplaceWith("this.subscribe(DefaultLifecycle, mode, consume)"))
@Suppress("ComposableParametersOrdering")
@Composable
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    mode: SubscriptionMode = SubscriptionMode.Started,
    consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> = subscribe(DefaultLifecycle, mode, consume)

/**
 * A function to subscribe to the store that follows the system lifecycle.
 *
 * * This function will assign the store a new subscriber when invoked, then populate the returned [State] with new states.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 *
 * @param mode the subscription mode that should be reached in order to subscribe to the store. At specified moments
 * in the UI lifecycle (Activity, Composable, Window etc), the store will subscribe and unsubscribe from the store.
 * @return the [State] that contains the current state.
 * @see ImmutableStore.subscribe
 * @see subscribe
 */
@Deprecated(Message, ReplaceWith("this.subscribe(DefaultLifecycle, mode)"))
@Composable
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    mode: SubscriptionMode = SubscriptionMode.Started,
): State<S> = subscribe(DefaultLifecycle, mode)
