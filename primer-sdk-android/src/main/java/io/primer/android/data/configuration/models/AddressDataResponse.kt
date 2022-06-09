package io.primer.android.data.configuration.models

import io.primer.android.domain.action.models.PrimerAddress
import kotlinx.serialization.Serializable

@Serializable
internal data class AddressDataResponse(
    val firstName: String? = null,
    val lastName: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val countryCode: CountryCode? = null,
) {
    fun toAddress() = PrimerAddress(
        firstName,
        lastName,
        addressLine1,
        addressLine2,
        postalCode,
        city,
        state,
        countryCode
    )
}
