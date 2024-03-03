package pro.respawn.flowmvi.debugger.server.ui

import androidx.compose.runtime.Stable
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.EventType

@Stable
val ClientEvent.type
    get() = when (this) {
        is ClientEvent.StoreAction -> EventType.Action
        is ClientEvent.StoreException -> EventType.Exception
        is ClientEvent.StoreIntent -> EventType.Intent
        is ClientEvent.StoreStateChanged -> EventType.StateChange
        is ClientEvent.StoreStarted, is ClientEvent.StoreStopped -> EventType.Connection
        is ClientEvent.StoreSubscribed, is ClientEvent.StoreUnsubscribed -> EventType.Subscription
    }

// TODO: Need a custom layout for some events, create a composable
@Stable
val ClientEvent.representation: String
    get() = when (this) {
        is ClientEvent.StoreAction -> "$name\n${data.prettyPrintToString()}"
        is ClientEvent.StoreException -> "$name\n$message\n$stackTrace"
        is ClientEvent.StoreIntent -> "$name\n${data.prettyPrintToString()}"
        is ClientEvent.StoreStarted -> name
        is ClientEvent.StoreStateChanged -> """
            ${from.name} -> ${to.name}
            
            ${from.body.prettyPrintToString()} 
            ->
            ${to.body.prettyPrintToString()}
        """.trimIndent()
        is ClientEvent.StoreStopped -> name
        is ClientEvent.StoreSubscribed -> "Subscription count changed to $newSubscriptionCount}"
        is ClientEvent.StoreUnsubscribed -> "Subscription count changed to $newSubscriptionCount"
    }
