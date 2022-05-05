package io.primer.android.data.configuration.model

import io.primer.android.domain.action.models.Address
import io.primer.android.model.dto.CountryCode
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
    fun toAddress() = Address(
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
