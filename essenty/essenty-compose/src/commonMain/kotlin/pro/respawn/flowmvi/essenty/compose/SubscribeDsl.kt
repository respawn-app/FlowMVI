@file:MustUseReturnValues

package pro.respawn.flowmvi.essenty.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableStore
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
 * An alias for [subscribe] that uses the provided Essenty [LifecycleOwner] for subscription.
 *
 * See the parent function documentation for more details on how the composable subscribes to the store.
 * @see subscribe
 */
@Suppress("ComposableParametersOrdering")
@JvmName("subscribe3")
@Composable
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    consume: suspend CoroutineScope.(action: A) -> Unit,
): State<S> = subscribe(
    lifecycle = rememberSubscriberLifecycle(lifecycleOwner) { lifecycle.asSubscriberLifecycle },
    mode = lifecycleState.asSubscriptionMode,
    consume = consume
)

/**
 * An alias for [subscribe] that uses the provided Essenty [LifecycleOwner] for subscription.
 *
 * See the parent function documentation for more details on how the composable subscribes to the store.
 * @see subscribe
 */
@Composable
@FlowMVIDSL
@JvmName("subscribe4")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.subscribe(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
): State<S> = subscribe(
    lifecycle = rememberSubscriberLifecycle(lifecycleOwner) { lifecycle.asSubscriberLifecycle },
    mode = lifecycleState.asSubscriptionMode
)
