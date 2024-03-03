@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.debugger.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.debugger.serializers.UUIDSerializer

@Serializable
data class StoreConnectionDescriptor(
    val storeId: Uuid,
    val storeName: String,
)
