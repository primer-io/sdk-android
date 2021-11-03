package io.primer.android.payment.google

import androidx.annotation.Keep
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.model.dto.PaymentMethodType
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Keep
@Serializable
data class GooglePay(
    val merchantName: String? = null,
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
    val buttonStyle: ButtonStyle = ButtonStyle.BLACK,
) : PaymentMethod {

    companion object {
        enum class ButtonStyle {
            WHITE,
            BLACK,
        }
    }

    override val type = PaymentMethodType.GOOGLE_PAY

    override val canBeVaulted: Boolean = false

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    internal val billingAddressRequired: Boolean = false

    override val module: PaymentMethodModule by lazy { GooglePayModule() }

    override val serializersModule: SerializersModule
        get() = googlePaySerializationModule
}

private val googlePaySerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(GooglePay::class, GooglePay.serializer())
    }
}
