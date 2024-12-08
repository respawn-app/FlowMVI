@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty") // response models for internal usage

package pro.respawn.flowmvi.debugger.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.name
import kotlin.uuid.Uuid

@Serializable
@SerialName("client")
public sealed interface ClientEvent : MVIIntent {

    @Serializable
    @SerialName("connected")
    public data class StoreConnected(
        val name: String,
        val id: Uuid,
    ) : ClientEvent

    @Serializable
    @SerialName("disconnected")
    public data class StoreDisconnected(
        val id: Uuid,
    ) : ClientEvent

    @Serializable
    @SerialName("started")
    public data class StoreStarted(
        val name: String,
    ) : ClientEvent

    @Serializable
    @SerialName("stopped")
    public data class StoreStopped(
        val name: String,
    ) : ClientEvent

    @Serializable
    @SerialName("intent")
    public data class StoreIntent(
        val name: String,
        val data: String,
    ) : ClientEvent {

        public constructor(intent: MVIIntent) : this(name = intent.name, intent.toString())
    }

    @Serializable
    @SerialName("action")
    public data class StoreAction(
        val name: String,
        val data: String,
    ) : ClientEvent {

        public constructor(action: MVIAction) : this(name = action.name, action.toString())
    }

    @Serializable
    @SerialName("state_change")
    public data class StoreStateChanged(
        val from: StoreState,
        val to: StoreState,
    ) : ClientEvent {

        public constructor(from: MVIState, to: MVIState) : this(from = StoreState(from), to = StoreState(to))
    }

    @Serializable
    @SerialName("unsubscribed")
    public data class StoreUnsubscribed(
        val newSubscriptionCount: Int,
    ) : ClientEvent

    @Serializable
    @SerialName("subscribed")
    public data class StoreSubscribed(
        val newSubscriptionCount: Int,
    ) : ClientEvent

    @Serializable
    @SerialName("exception")
    public data class StoreException(
        val name: String,
        val message: String?,
        val stackTrace: String,
    ) : ClientEvent {

        public constructor(e: Exception) : this(e.name, e.message, e.stackTraceToString())
    }
}
