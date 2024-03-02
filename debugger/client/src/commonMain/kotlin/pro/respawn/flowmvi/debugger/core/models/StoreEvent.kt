@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.debugger.core.models

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.debugger.core.serializers.UUIDSerializer

@Serializable
internal data class StoreEvent(
    val event: OutgoingEvent,
    val storeId: Uuid,
)
