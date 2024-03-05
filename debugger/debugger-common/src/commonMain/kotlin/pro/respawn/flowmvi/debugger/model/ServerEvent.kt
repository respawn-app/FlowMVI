@file:UseSerializers(UUIDSerializer::class)
@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty") // response models for internal usage

package pro.respawn.flowmvi.debugger.model

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.debugger.serializers.UUIDSerializer

@Serializable
public sealed interface ServerEvent : MVIIntent {

    public val storeId: Uuid

    @Serializable
    public data class Stop(val index: Int, override val storeId: Uuid) : ServerEvent
}
