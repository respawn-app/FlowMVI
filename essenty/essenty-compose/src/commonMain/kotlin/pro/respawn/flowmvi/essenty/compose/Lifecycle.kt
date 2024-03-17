package pro.respawn.flowmvi.essenty.compose

import androidx.compose.runtime.Stable
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.repeatOnLifecycle
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle
import pro.respawn.flowmvi.compose.api.SubscriptionMode
import pro.respawn.flowmvi.compose.dsl.SubscriberLifecycle

/**
 * Convert this [LifecycleOwner] to a [SubscriberLifecycle].
 */
@Stable
public val Lifecycle.asSubscriberLifecycle: SubscriberLifecycle
    get() = SubscriberLifecycle(this) { mode, block -> repeatOnLifecycle(mode.asEssentyLifecycle, block = block) }

/**
 * Convert this [SubscriptionMode] to an Essenty [Lifecycle.State]
 */
@Stable
public val SubscriptionMode.asEssentyLifecycle: Lifecycle.State
    get() = when (this) {
        SubscriptionMode.Immediate -> Lifecycle.State.CREATED
        SubscriptionMode.Started -> Lifecycle.State.STARTED
        SubscriptionMode.Visible -> Lifecycle.State.RESUMED
    }

/**
 * Convert this Essenty [Lifecycle.State] to a [SubscriptionMode].
 *
 * [Lifecycle.State.DESTROYED] and [Lifecycle.State.INITIALIZED] are not supported by
 * Essenty as valid subscription modes and wll throw an [IllegalStateException]
 */
public val Lifecycle.State.asSubscriptionMode: SubscriptionMode
    get() = when (this) {
        Lifecycle.State.CREATED -> SubscriptionMode.Immediate
        Lifecycle.State.STARTED -> SubscriptionMode.Started
        Lifecycle.State.RESUMED -> SubscriptionMode.Visible
        Lifecycle.State.DESTROYED,
        Lifecycle.State.INITIALIZED -> error("Essenty does not provide support for using $name as subscriber lifecycle")
    }
