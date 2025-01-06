package io.primer.android.googlepay

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal data class GooglePay(
    val merchantName: String? = null,
    val totalPrice: String,
    val countryCode: String,
    val currencyCode: String,
    val allowedCardNetworks: List<CardNetwork.Type>,
    val buttonStyle: GooglePayButtonStyle = GooglePayButtonStyle.BLACK,
    val billingAddressRequired: Boolean = false,
    val existingPaymentMethodRequired: Boolean = false,
) : PaymentMethod {
    override val type = PaymentMethodType.GOOGLE_PAY.name

    override val canBeVaulted: Boolean = false

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

    override val module: PaymentMethodModule by lazy { GooglePayModule() }

    companion object {
        val allowedCardNetworks: Set<CardNetwork.Type> =
            setOf(
                CardNetwork.Type.VISA,
                CardNetwork.Type.MASTERCARD,
                CardNetwork.Type.AMEX,
                CardNetwork.Type.DISCOVER,
                CardNetwork.Type.JCB,
            )
    }
}
