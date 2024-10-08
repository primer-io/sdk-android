package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.ui.CardNetwork

data class PrimerClientSession(
    val customerId: String?,
    val orderId: String?,
    val currencyCode: String?,
    val totalAmount: Int?,
    val lineItems: List<PrimerLineItem>?,
    val orderDetails: PrimerOrder?,
    val customer: PrimerCustomer?,
    val paymentMethod: PrimerPaymentMethod?,
    val fees: List<PrimerFee>?
)

data class PrimerCustomer(
    val emailAddress: String?,
    val mobileNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val billingAddress: PrimerAddress?,
    val shippingAddress: PrimerAddress?
)

data class PrimerOrder(
    val countryCode: CountryCode?,
    val shipping: PrimerShipping?
)

data class PrimerShipping(
    val amount: Int?,
    val methodId: String?,
    val methodName: String?,
    val methodDescription: String?
)

data class PrimerLineItem(
    val itemId: String?,
    val itemDescription: String?,
    val amount: Int?,
    val discountAmount: Int?,
    val quantity: Int?,
    val taxCode: String?,
    val taxAmount: Int?
)

data class PrimerAddress(
    val firstName: String? = null,
    val lastName: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val countryCode: CountryCode? = null
) {
    val country: String?
        get() = countryCode?.name
}

data class PrimerPaymentMethod(
    val orderedAllowedCardNetworks: List<CardNetwork.Type>
)

data class PrimerFee(
    val type: String?,
    val amount: Int
)
