@file:MustUseReturnValues

package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import pro.respawn.flowmvi.api.SubscriberLifecycle
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.dsl.SubscriberLifecycle

/**
 * Converts this Android [LifecycleOwner] to a [SubscriberLifecycle].
 */
@Stable
@get:RememberInComposition
internal val Lifecycle.asSubscriberLifecycle: SubscriberLifecycle
    get() = SubscriberLifecycle(this) { mode, block ->
        repeatOnLifecycle(mode.asLifecycleState, block)
    }

/**
 * Converts this [SubscriptionMode] to a [Lifecycle.State]
 */
@Stable
public val SubscriptionMode.asLifecycleState: Lifecycle.State
    get() = when (this) {
        SubscriptionMode.Immediate -> Lifecycle.State.CREATED
        SubscriptionMode.Started -> Lifecycle.State.STARTED
        SubscriptionMode.Visible -> Lifecycle.State.RESUMED
    }

/**
 * Converts the [Lifecycle.State] to a [SubscriptionMode]
 *
 * [Lifecycle.State.DESTROYED] and [Lifecycle.State.INITIALIZED] **cannot** be used as valid subscription modes as
 * it is not supported by Android
 */
@Stable
public val Lifecycle.State.asSubscriptionMode: SubscriptionMode
    get() = when (this) {
        Lifecycle.State.CREATED -> SubscriptionMode.Immediate
        Lifecycle.State.STARTED -> SubscriptionMode.Started
        Lifecycle.State.RESUMED -> SubscriptionMode.Visible
        Lifecycle.State.DESTROYED,
        Lifecycle.State.INITIALIZED -> error("Android lifecycle does not support $this as subscription mode")
    }
