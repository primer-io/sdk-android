package io.primer.android.model.dto

import io.primer.android.UXMode
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.UserDetails
import io.primer.android.threeds.data.models.ThreeDsAmount
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
    val threeDsAmount: ThreeDsAmount,
    val currency: String?,
    val isStandalonePaymentMethod: Boolean,
    val doNotShowUi: Boolean,
    val theme: UniversalCheckoutTheme,
    val preferWebView: Boolean,
    val is3DSAtTokenizationEnabled: Boolean,
    val debugOptions: PrimerDebugOptions?,
    val orderId: String?,
    val userDetails: UserDetails?,
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
        is3DSAtTokenizationEnabled: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        userDetails: UserDetails? = null,
    ) : this(
        clientToken = clientToken,
        packageName = packageName,
        uxMode = uxMode,
        locale = locale,
        doNotShowUi = doNotShowUi,
        countryCode = countryCode,
        monetaryAmount = MonetaryAmount.create(currency = currency, value = amount),
        threeDsAmount = ThreeDsAmount(amount, currency),
        currency = currency,
        isStandalonePaymentMethod = isStandalonePaymentMethod,
        theme = theme ?: UniversalCheckoutTheme.getDefault(),
        preferWebView = preferWebView,
        is3DSAtTokenizationEnabled = is3DSAtTokenizationEnabled,
        debugOptions = debugOptions,
        orderId = orderId,
        userDetails = userDetails
    )
}

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
