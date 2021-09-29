package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
class Address(
    val line1: String,
    val line2: String? = null,
    val postalCode: String,
    val city: String,
    val countryCode: CountryCode,
) {
    val country: String
        get() = countryCode.name

    fun toAddressLine(): String {
        val values = listOf(line1, line2, postalCode, city, country)
        return values.joinToString(", ")
    }
}
