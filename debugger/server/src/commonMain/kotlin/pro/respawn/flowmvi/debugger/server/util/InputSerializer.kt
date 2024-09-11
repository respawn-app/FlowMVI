package pro.respawn.flowmvi.debugger.server.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.dsl.input

object InputSerializer : KSerializer<Input> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Input", STRING)

    override fun deserialize(decoder: Decoder): Input = decoder.decodeString().input()

    override fun serialize(encoder: Encoder, value: Input) = encoder.encodeString(value.value)
}
