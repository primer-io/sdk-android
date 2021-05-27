package io.primer.android.model.dto

import io.primer.android.UXMode
import io.primer.android.UniversalCheckoutTheme
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Locale

@Serializable
data class CheckoutConfig(
    val clientToken: String,
    val packageName: String,
    val uxMode: UXMode,
    @Serializable(with = LocaleSerializer::class) val locale: Locale,
    val countryCode: CountryCode?,
    val monetaryAmount: MonetaryAmount?,
    val isStandalonePaymentMethod: Boolean,
    val doNotShowUi: Boolean,
    val theme: UniversalCheckoutTheme,
    val preferWebView: Boolean,
) {

    // FIXME move Locale to Klarna
    constructor(
        clientToken: String,
        packageName: String,
        locale: Locale,
        countryCode: CountryCode? = null,
        uxMode: UXMode = UXMode.CHECKOUT,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        currency: String? = null,
        amount: Int? = null,
        theme: UniversalCheckoutTheme? = null,
        preferWebView: Boolean = false,
    ) : this(
        clientToken = clientToken,
        packageName = packageName,
        uxMode = uxMode,
        locale = locale,
        doNotShowUi = doNotShowUi,
        countryCode = countryCode,
        monetaryAmount = MonetaryAmount.create(currency = currency, value = amount),
        isStandalonePaymentMethod = isStandalonePaymentMethod,
        theme = theme ?: UniversalCheckoutTheme.getDefault(),
        preferWebView = preferWebView,
    )
}

object LocaleSerializer : KSerializer<Locale> {

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
