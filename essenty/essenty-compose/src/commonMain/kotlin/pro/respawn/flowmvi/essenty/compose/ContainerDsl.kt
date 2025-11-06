@file:MustUseReturnValue

package pro.respawn.flowmvi.essenty.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableContainer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.compose.dsl.rememberSubscriberLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.dsl.subscribe
import pro.respawn.flowmvi.essenty.lifecycle.asSubscriberLifecycle
import pro.respawn.flowmvi.essenty.lifecycle.asSubscriptionMode
import kotlin.jvm.JvmName

/**
 * An alias for [subscribe] that subscribes to `this` [Container] using its lifecycle.
 *
 * See the parent function documentation for more details on how the composable subscribes to the store.
 * @see subscribe
 */
@Composable
@FlowMVIDSL
@JvmName("subscribe1")
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
): State<S> where T : LifecycleOwner, T : ImmutableContainer<S, I, A> = store.subscribe(
    lifecycle = rememberSubscriberLifecycle(this) { lifecycle.asSubscriberLifecycle },
    mode = lifecycleState.asSubscriptionMode,
)

/**
 * An alias for [subscribe] that subscribes to `this` [Container] using its lifecycle.
 *
 * See the parent function documentation for more details on how the composable subscribes to the store.
 * @see subscribe
 */
@Composable
@FlowMVIDSL
@JvmName("subscribe2")
@Suppress("ComposableParametersOrdering")
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> where T : LifecycleOwner, T : ImmutableContainer<S, I, A> = store.subscribe(
    lifecycle = rememberSubscriberLifecycle(this) { lifecycle.asSubscriberLifecycle },
    mode = lifecycleState.asSubscriptionMode,
    consume = consume,
)
