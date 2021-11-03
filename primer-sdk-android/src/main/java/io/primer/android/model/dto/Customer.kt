package io.primer.android.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    @SerialName("customerId") val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    @SerialName("emailAddress") val email: String? = null,
    val homePhone: String? = null,
    @SerialName("mobileNumber") val mobilePhone: String? = null,
    val workPhone: String? = null,
    val billingAddress: Address? = null,
    val shippingAddress: Address? = null,
) {
    internal fun areDetailsAvailable() =
        firstName != null || lastName != null || billingAddress != null
}
