package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class Address(
    val title: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val addressLine1: String,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val city: String,
    val state: String? = null,
    val countryCode: String,
    val postalCode: String,
)
