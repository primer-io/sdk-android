package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data class GooglePay(
    val merchantName: String,
    val totalPrice: String,
    val countryCode: String,
    val currencyCode: String,
    val allowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
    val buttonStyle: ButtonStyle = ButtonStyle.WHITE,
) : PaymentMethod {

    companion object {
        enum class ButtonStyle {
            WHITE,
            BLACK,
//            BORDER,
        }
    }

    override val identifier: String = GOOGLE_PAY_IDENTIFIER

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    internal val billingAddressRequired: Boolean = false

    override val module: PaymentMethodModule by lazy { GooglePayModule() }

    override val serializersModule: SerializersModule
        get() = googlePaySerializationModule
}

private val googlePaySerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(GooglePay::class)
    }
}
