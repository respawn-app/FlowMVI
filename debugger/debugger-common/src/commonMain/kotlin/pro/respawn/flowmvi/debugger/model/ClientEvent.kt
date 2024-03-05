@file:UseSerializers(UUIDSerializer::class)
@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty") // response models for internal usage

package pro.respawn.flowmvi.debugger.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.name
import pro.respawn.flowmvi.debugger.serializers.UUIDSerializer

@Serializable
public sealed interface ClientEvent : MVIIntent {

    @Serializable
    public data class StoreConnected(
        val name: String,
        val id: Uuid,
    ) : ClientEvent

    @Serializable
    public data class StoreDisconnected(
        val id: Uuid,
    ) : ClientEvent

    @Serializable
    public data class StoreStarted(
        val name: String,
    ) : ClientEvent

    @Serializable
    public data class StoreStopped(
        val name: String,
    ) : ClientEvent

    @Serializable
    public data class StoreIntent(
        val name: String,
        val data: String,
    ) : ClientEvent {

        public constructor(intent: MVIIntent) : this(name = intent.name, intent.toString())
    }

    @Serializable
    public data class StoreAction(
        val name: String,
        val data: String,
    ) : ClientEvent {

        public constructor(action: MVIAction) : this(name = action.name, action.toString())
    }

    @Serializable
    public data class StoreStateChanged(
        val from: StoreState,
        val to: StoreState,
    ) : ClientEvent {

        public constructor(from: MVIState, to: MVIState) : this(from = StoreState(from), to = StoreState(to))
    }

    @Serializable
    public data class StoreUnsubscribed(
        val newSubscriptionCount: Int,
    ) : ClientEvent

    @Serializable
    public data class StoreSubscribed(
        val newSubscriptionCount: Int,
    ) : ClientEvent

    @Serializable
    public data class StoreException(
        val name: String,
        val message: String?,
        val stackTrace: String,
    ) : ClientEvent {

        public constructor(e: Exception) : this(e.name, e.message, e.stackTraceToString())
    }
}
