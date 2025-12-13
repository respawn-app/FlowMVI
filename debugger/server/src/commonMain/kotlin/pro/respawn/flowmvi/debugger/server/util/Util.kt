package pro.respawn.flowmvi.debugger.server.util

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.EventType

fun <T> PersistentList<T>?.orEmpty() = this ?: persistentListOf()

@Stable
internal val ClientEvent.type
    get() = when (this) {
        is ClientEvent.StoreAction -> EventType.Action
        is ClientEvent.StoreException -> EventType.Exception
        is ClientEvent.StoreIntent -> EventType.Intent
        is ClientEvent.StoreStateChanged -> EventType.StateChange
        is ClientEvent.StoreStarted,
        is ClientEvent.StoreStopped -> EventType.Initialization
        is ClientEvent.StoreSubscribed,
        is ClientEvent.StoreUnsubscribed -> EventType.Subscription
        is ClientEvent.StoreDisconnected,
        is ClientEvent.StoreConnected -> EventType.Connection
    }

// TODO: Need a custom layout for some events, create a composable
@Stable
@Suppress("StringShouldBeRawString", "MultilineRawStringIndentation")
internal val ClientEvent.representation: String
    get() = when (this) {
        is ClientEvent.StoreConnected -> "Connected $name ($id)"
        is ClientEvent.StoreDisconnected -> "Disconnected $id"
        is ClientEvent.StoreAction -> data.prettyPrintToString()
        is ClientEvent.StoreIntent -> data.prettyPrintToString()
        is ClientEvent.StoreException -> "$name:\n\n$message\n$stackTrace"
        is ClientEvent.StoreStarted -> "$name started"
        is ClientEvent.StoreStopped -> "$name stopped"
        is ClientEvent.StoreSubscribed -> "Subscription count changed to $newSubscriptionCount"
        is ClientEvent.StoreUnsubscribed -> "Subscription count changed to $newSubscriptionCount"
        is ClientEvent.StoreStateChanged -> """
${from.name} ---> 

${from.body.prettyPrintToString()} 

<--- ${to.name}

${to.body.prettyPrintToString()}
        """.trimIndent()
    }
