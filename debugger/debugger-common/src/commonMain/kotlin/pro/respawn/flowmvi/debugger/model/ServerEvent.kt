@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.debugger.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.debugger.serializers.UUIDSerializer

@Serializable
sealed interface ServerEvent : MVIIntent {

    val storeId: Uuid

    @Serializable
    data class Stop(val index: Int, override val storeId: Uuid) : ServerEvent

    @Serializable
    data class RestoreState(val index: Int, override val storeId: Uuid) : ServerEvent
}
