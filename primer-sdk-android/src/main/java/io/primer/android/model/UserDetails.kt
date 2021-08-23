package io.primer.android.model

import io.primer.android.model.dto.CountryCode
import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    val firstName: String,
    val lastName: String,
    val email: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val postalCode: String,
    val countryCode: CountryCode,
    val homePhone: String? = null,
    val mobilePhone: String? = null,
    val workPhone: String? = null,
)
