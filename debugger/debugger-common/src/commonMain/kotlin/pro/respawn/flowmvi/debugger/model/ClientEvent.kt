@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty") // response models for internal usage

package pro.respawn.flowmvi.debugger.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.name
import kotlin.uuid.Uuid

// @Serializable
// public data class ClientMeta(
//     val name: String? = null,
// )

@Serializable
@SerialName("client")
public sealed interface ClientEvent : MVIIntent {

    public val storeName: String?

    @Serializable
    @SerialName("connected")
    public data class StoreConnected(
        val name: String?,
        val id: Uuid,
    ) : ClientEvent {

        override val storeName: String? = name
    }

    @Serializable
    @SerialName("disconnected")
    public data class StoreDisconnected(
        val id: Uuid,
    ) : ClientEvent {

        override val storeName: String? = null
    }

    @Serializable
    @SerialName("started")
    public data class StoreStarted(
        val name: String?,
    ) : ClientEvent {

        override val storeName: String? = name
    }

    @Serializable
    @SerialName("stopped")
    public data class StoreStopped(
        val name: String?,
        val id: Uuid,
    ) : ClientEvent {

        override val storeName: String? = name
    }

    @Serializable
    @SerialName("intent")
    public data class StoreIntent(
        val name: String,
        val data: String,
        override val storeName: String? = null,
    ) : ClientEvent {

        public constructor(
            intent: MVIIntent,
            storeName: String?,
        ) : this(
            name = intent.name,
            data = intent.toString(),
            storeName = storeName
        )
    }

    @Serializable
    @SerialName("action")
    public data class StoreAction(
        val name: String,
        val data: String,
        override val storeName: String? = null,
    ) : ClientEvent {

        public constructor(
            action: MVIAction,
            storeName: String?
        ) : this(
            name = action.name,
            action.toString(),
            storeName = storeName
        )
    }

    @Serializable
    @SerialName("state_change")
    public data class StoreStateChanged(
        val from: StoreState,
        val to: StoreState,
        override val storeName: String? = null,
    ) : ClientEvent {

        public constructor(from: MVIState, to: MVIState, name: String?) : this(
            from = StoreState(from),
            to = StoreState(to),
            storeName = name,
        )
    }

    @Serializable
    @SerialName("unsubscribed")
    public data class StoreUnsubscribed(
        val newSubscriptionCount: Int,
        override val storeName: String? = null,
    ) : ClientEvent

    @Serializable
    @SerialName("subscribed")
    public data class StoreSubscribed(
        val newSubscriptionCount: Int,
        override val storeName: String? = null,
    ) : ClientEvent

    @Serializable
    @SerialName("exception")
    public data class StoreException(
        val name: String,
        val message: String?,
        val stackTrace: String,
        override val storeName: String? = null,
    ) : ClientEvent {

        public constructor(e: Exception, name: String?) : this(e.name, e.message, e.stackTraceToString(), name)
    }
}
