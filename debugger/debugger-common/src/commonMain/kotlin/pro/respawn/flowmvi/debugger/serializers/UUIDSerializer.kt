package pro.respawn.flowmvi.debugger.serializers

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object UUIDSerializer : KSerializer<Uuid> {

    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = uuidFrom(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Uuid) = encoder.encodeString(value.toString())
}
