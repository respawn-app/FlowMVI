@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty") // response models for internal usage

package pro.respawn.flowmvi.debugger.model

import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction

@Serializable
@SerialName("ServerEvent")
public sealed interface ServerEvent : MVIAction {

    public val storeId: Uuid

    @Serializable
    @SerialName("Stop")
    public data class Stop(override val storeId: Uuid) : ServerEvent

    @Serializable
    @SerialName("ResendLastIntent")
    public data class ResendLastIntent(override val storeId: Uuid) : ServerEvent

    @Serializable
    @SerialName("RollbackState")
    public data class RollbackState(override val storeId: Uuid) : ServerEvent

    @Serializable
    @SerialName("ResendLastAction")
    public data class ResendLastAction(override val storeId: Uuid) : ServerEvent

    @Serializable
    @SerialName("RethrowLastException")
    public data class RethrowLastException(override val storeId: Uuid) : ServerEvent

    @Serializable
    @SerialName("RollbackToInitialState")
    public data class RollbackToInitialState(override val storeId: Uuid) : ServerEvent
}
