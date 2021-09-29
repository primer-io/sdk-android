package io.primer.android.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Locale

internal object LocaleSerializer : KSerializer<Locale> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "Locale",
        PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): Locale {
        val language = decoder.decodeString()
        val country = decoder.decodeString()
        val variant = decoder.decodeString()
        return Locale(language, country, variant)
    }

    override fun serialize(encoder: Encoder, value: Locale) {
        encoder.encodeString(value.language)
        encoder.encodeString(value.country)
        encoder.encodeString(value.variant)
    }
}
