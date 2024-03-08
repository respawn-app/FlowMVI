package pro.respawn.flowmvi.compose.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import pro.respawn.flowmvi.compose.api.SubscriberLifecycleOwner
import pro.respawn.flowmvi.compose.api.SubscriptionMode

public fun LifecycleOwner.asSubscriberOwner(): SubscriberLifecycleOwner = SubscriberLifecycleOwner { mode, block ->
    repeatOnLifecycle(mode.asLifecycleState, block)
}

public val SubscriptionMode.asLifecycleState: Lifecycle.State
    get() = when (this) {
        SubscriptionMode.Immediate -> Lifecycle.State.CREATED
        SubscriptionMode.Started -> Lifecycle.State.STARTED
        SubscriptionMode.Visible -> Lifecycle.State.RESUMED
    }

public val Lifecycle.State.asSubscriptionMode: SubscriptionMode
    get() = when (this) {
        Lifecycle.State.CREATED -> SubscriptionMode.Immediate
        Lifecycle.State.STARTED -> SubscriptionMode.Started
        Lifecycle.State.RESUMED -> SubscriptionMode.Visible
        Lifecycle.State.DESTROYED,
        Lifecycle.State.INITIALIZED -> error("Android lifecycle does not support $this as subscription mode")
    }
