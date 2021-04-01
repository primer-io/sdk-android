package io.primer.android

import io.primer.android.model.OrderItem
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.payment.KLARNA_IDENTIFIER
import io.primer.android.payment.PAYMENT_CARD_IDENTIFIER
import io.primer.android.payment.PAYPAL_IDENTIFIER
import kotlinx.serialization.Serializable

// FIXME these can't be sealed if we want to modularize (consider marker interface)
@Serializable
sealed class PaymentMethod(val identifier: String) {

    @Serializable
    class Card : PaymentMethod(PAYMENT_CARD_IDENTIFIER)

    @Serializable
    class PayPal : PaymentMethod(PAYPAL_IDENTIFIER)

    @Serializable
    class Klarna(
        val amount: Int,
        val currency: String,
        val orderItems: List<OrderItem>,
    ) : PaymentMethod(KLARNA_IDENTIFIER)

    @Serializable
    class GoCardless(
        val companyName: String,
        val companyAddress: String,
        val customerName: String? = null,
        val customerEmail: String? = null,
        val customerAddressLine1: String? = null,
        val customerAddressLine2: String? = null,
        val customerAddressCity: String? = null,
        val customerAddressState: String? = null,
        val customerAddressCountryCode: String? = null,
        val customerAddressPostalCode: String? = null,
    ) : PaymentMethod(GOCARDLESS_IDENTIFIER)
}
