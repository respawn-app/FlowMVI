@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.debugger.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.serializers.UUIDSerializer
import pro.respawn.flowmvi.debugger.name

@Serializable
sealed interface ClientEvent : MVIIntent {

    @Serializable
    data class StoreStarted(
        val name: String,
    ) : ClientEvent

    @Serializable
    data class StoreStopped(
        val name: String,
    ) : ClientEvent

    @Serializable
    data class StoreIntent(
        val name: String,
        val data: String,
    ) : ClientEvent {

        constructor(intent: MVIIntent) : this(name = intent.name, intent.toString())
    }

    @Serializable
    data class StoreAction(
        val name: String,
        val data: String,
    ) : ClientEvent {

        constructor(action: MVIAction) : this(name = action.name, action.toString())
    }

    @Serializable
    data class StoreStateChanged(
        val from: StoreState,
        val to: StoreState,
    ) : ClientEvent {

        constructor(from: MVIState, to: MVIState) : this(from = StoreState(from), to = StoreState(to))
    }

    @Serializable
    data class StoreUnsubscribed(
        val newSubscriptionCount: Int,
    ) : ClientEvent

    @Serializable
    data class StoreSubscribed(
        val newSubscriptionCount: Int,
    ) : ClientEvent

    @Serializable
    data class StoreException(
        val name: String,
        val message: String?,
        val stackTrace: String,
    ) : ClientEvent {
        constructor(e: Exception): this(e.name, e.message, e.stackTraceToString())
    }
}
