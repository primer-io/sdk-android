package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val homePhone: String? = null,
    val mobilePhone: String? = null,
    val workPhone: String? = null,
    val billingAddress: Address? = null,
    val shippingAddress: Address? = null,
) {
    val detailsAvailable: Boolean
        get() {
            return firstName != null || lastName != null || billingAddress != null
        }
}
