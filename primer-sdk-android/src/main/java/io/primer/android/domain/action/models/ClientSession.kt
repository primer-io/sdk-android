package io.primer.android.domain.action.models

import io.primer.android.model.dto.CountryCode
import kotlinx.serialization.Serializable

data class ClientSession(
    val customerId: String?,
    val orderId: String?,
    val currencyCode: String?,
    val totalAmount: Int?,
    val lineItems: List<LineItem>?,
    val orderDetails: Order?,
    val customer: Customer?,
)

data class Customer(
    val emailAddress: String?,
    val mobileNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val billingAddress: Address?,
    val shippingAddress: Address?,
)

data class Order(
    val countryCode: CountryCode?,
)

data class LineItem(
    val itemId: String?,
    val itemDescription: String?,
    val amount: Int?,
    val discountAmount: Int?,
    val quantity: Int?,
    val taxCode: String?,
    val taxAmount: Int?
)

@Serializable
data class Address(
    val firstName: String? = null,
    val lastName: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val countryCode: CountryCode? = null,
) {
    val country: String?
        get() = countryCode?.name

    fun toAddressLine(): String {
        val values = listOf(addressLine1, addressLine2, postalCode, city, country)
        return values.joinToString(", ")
    }
}
