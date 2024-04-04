package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.ui.CardNetwork

internal data class GooglePay(
    val merchantName: String? = null,
    val totalPrice: String,
    val countryCode: String,
    val currencyCode: String,
    val allowedCardNetworks: List<CardNetwork.Type>,
    val buttonStyle: GooglePayButtonStyle = GooglePayButtonStyle.BLACK,
    val billingAddressRequired: Boolean = false,
    val existingPaymentMethodRequired: Boolean = false
) : PaymentMethod {

    override val type = PaymentMethodType.GOOGLE_PAY.name

    override val canBeVaulted: Boolean = false

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

    override val module: PaymentMethodModule by lazy { GooglePayModule() }

    companion object {

        val allowedCardNetworks = setOf(
            CardNetwork.Type.VISA,
            CardNetwork.Type.MASTERCARD,
            CardNetwork.Type.AMEX,
            CardNetwork.Type.DISCOVER,
            CardNetwork.Type.JCB
        )
    }
}
