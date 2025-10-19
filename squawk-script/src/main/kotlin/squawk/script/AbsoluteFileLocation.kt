package squawk.script

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

data object AbsoluteFileLocation: KSerializer<File>
{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("absoluteFilePath", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: File)
    {
        encoder.encodeString(value.absolutePath)
    }

    override fun deserialize(decoder: Decoder): File
    {
        return File(decoder.decodeString())
    }
}
