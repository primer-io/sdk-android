package io.primer.android.model.dto

import io.primer.android.UXMode
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.model.OrderItem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable
internal data class CheckoutConfig(
    val clientToken: String,
    val packageName: String,
    val uxMode: UXMode,
    @Serializable(with = LocaleSerializer::class) val locale: Locale,
    val amount: MonetaryAmount?,
//    val orderItems: List<OrderItem>,
    val isStandalonePayment: Boolean,
    val theme: UniversalCheckoutTheme,
) {

    constructor(
        clientToken: String,
        packageName: String,
        locale: Locale,
        uxMode: UXMode = UXMode.CHECKOUT,
        isStandalonePayment: Boolean = false,
        currency: String? = null,
        amount: Int? = null,
//        orderItems: List<OrderItem> = emptyList(),
        theme: UniversalCheckoutTheme? = null,
    ) : this(
        clientToken = clientToken,
        packageName = packageName,
        uxMode = uxMode,
        locale = locale,
        amount = MonetaryAmount.create(currency = currency, value = amount),
//        orderItems = orderItems,
        isStandalonePayment = isStandalonePayment,
        theme = theme ?: UniversalCheckoutTheme.getDefault()
    )
}

object LocaleSerializer : KSerializer<Locale> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

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
