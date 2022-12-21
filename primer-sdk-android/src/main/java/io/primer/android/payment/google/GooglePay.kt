package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.GooglePayButtonStyle

internal data class GooglePay(
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
    val buttonStyle: GooglePayButtonStyle = GooglePayButtonStyle.BLACK,
    val billingAddressRequired: Boolean = false
) : PaymentMethod {

    override val type = PaymentMethodType.GOOGLE_PAY.name

    override val canBeVaulted: Boolean = false

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

    override val module: PaymentMethodModule by lazy { GooglePayModule() }
}
