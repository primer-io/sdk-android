package io.primer.android

import io.primer.android.model.OrderItem
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
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
    data class GooglePay(
        val allowedCardNetworks: List<String> = listOf(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA"
        ),
    ) : PaymentMethod(GOOGLE_PAY_IDENTIFIER) {

        internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        internal val billingAddressRequired: Boolean = false
    }

    @Serializable
    data class Klarna(
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

// this new way of declaring PaymentMethods is meant to improve modularisation as each method can
// be declared in a separate module (provided the marker interface is declared in shared one)
interface _PaymentMethod

@Serializable
object _Card : _PaymentMethod

@Serializable
object _PayPal : _PaymentMethod

@Serializable
data class _GooglePay(
    val allowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
) : _PaymentMethod {

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    internal val billingAddressRequired: Boolean = false
}

@Serializable
data class _Klarna(val orderItems: List<OrderItem>) : _PaymentMethod

@Serializable
data class _GoCardless(
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
) : _PaymentMethod
