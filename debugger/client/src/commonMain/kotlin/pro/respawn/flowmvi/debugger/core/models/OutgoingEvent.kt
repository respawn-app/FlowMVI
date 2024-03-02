@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.debugger.core.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.core.name
import pro.respawn.flowmvi.debugger.core.serializers.UUIDSerializer

@Serializable
internal sealed interface OutgoingEvent {

    @Serializable
    data class StoreStarted(
        val name: String,
    ) : OutgoingEvent

    @Serializable
    data class StoreStopped(
        val name: String,
    ) : OutgoingEvent

    @Serializable
    data class StoreIntent(
        val name: String,
        val data: String,
    ) : OutgoingEvent {

        constructor(intent: MVIIntent) : this(name = intent.name, intent.toString())
    }

    @Serializable
    data class StoreAction(
        val name: String,
        val data: String,
    ) : OutgoingEvent {

        constructor(action: MVIAction) : this(name = action.name, action.toString())
    }

    @Serializable
    data class StoreStateChanged(
        val from: StoreState,
        val to: StoreState,
    ) : OutgoingEvent {

        constructor(from: MVIState, to: MVIState) : this(from = StoreState(from), to = StoreState(to))
    }

    @Serializable
    data class StoreUnsubscribed(
        val newSubscriptionCount: Int,
    ) : OutgoingEvent

    @Serializable
    data class StoreSubscribed(
        val newSubscriptionCount: Int,
    ) : OutgoingEvent

    @Serializable
    data class StoreException(
        val name: String,
        val message: String?,
        val stackTrace: String?,
    ) : OutgoingEvent {
        constructor(e: Exception): this(e.name, e.message, e.stackTraceToString())
    }
}
