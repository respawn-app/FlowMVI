@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.debugger.core.models

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.debugger.core.serializers.UUIDSerializer

@Serializable
internal sealed interface IncomingEvent {

    val storeId: Uuid

    @Serializable
    data class Stop(val index: Int, override val storeId: Uuid) : IncomingEvent

    @Serializable
    data class RestoreState(val index: Int, override val storeId: Uuid) : IncomingEvent
}
