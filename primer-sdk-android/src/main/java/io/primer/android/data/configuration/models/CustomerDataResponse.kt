package io.primer.android.data.configuration.models

import io.primer.android.domain.action.models.PrimerCustomer
import kotlinx.serialization.Serializable

@Serializable
internal data class CustomerDataResponse(
    val customerId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val emailAddress: String? = null,
    val homePhone: String? = null,
    val mobileNumber: String? = null,
    val workPhone: String? = null,
    val nationalDocumentId: String? = null,
    val billingAddress: AddressDataResponse? = null,
    val shippingAddress: AddressDataResponse? = null,
) {

    fun toCustomer() = PrimerCustomer(
        emailAddress,
        mobileNumber,
        firstName,
        lastName,
        billingAddress?.toAddress(),
        shippingAddress?.toAddress()
    )
}
