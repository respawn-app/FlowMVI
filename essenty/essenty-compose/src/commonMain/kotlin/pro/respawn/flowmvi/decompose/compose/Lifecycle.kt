package pro.respawn.flowmvi.decompose.compose

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.repeatOnLifecycle
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle
import pro.respawn.flowmvi.compose.api.SubscriptionMode

public val LifecycleOwner.asSubscriberLifecycle: SubscriberLifecycle
    get() = SubscriberLifecycle { mode, block -> repeatOnLifecycle(mode.asEssentyLifecycle, block = block) }

public val SubscriptionMode.asEssentyLifecycle: Lifecycle.State
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
        Lifecycle.State.INITIALIZED -> error("Essenty does not provide support for using $name as subscriber lifecycle")
    }
