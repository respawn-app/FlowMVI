package pro.respawn.flowmvi.debugger.serializers

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for the internal flowmvi UUID type
 */
public object UUIDSerializer : KSerializer<Uuid> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "com.benasher44.uuid.UUID",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): Uuid = uuidFrom(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Uuid): Unit = encoder.encodeString(value.toString())
}
