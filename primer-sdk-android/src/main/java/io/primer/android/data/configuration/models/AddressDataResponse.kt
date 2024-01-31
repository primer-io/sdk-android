package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.domain.action.models.PrimerAddress
import org.json.JSONObject

internal data class AddressDataResponse(
    val firstName: String?,
    val lastName: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val postalCode: String?,
    val city: String?,
    val state: String?,
    val countryCode: CountryCode?
) : JSONDeserializable {
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

    companion object {

        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"
        private const val ADDRESS_LINE_1_FIELD = "addressLine1"
        private const val ADDRESS_LINE_2_FIELD = "addressLine2"
        private const val CITY_FIELD = "city"
        private const val STATE_FIELD = "state"
        private const val COUNTRY_CODE_FIELD = "countryCode"
        private const val POSTAL_CODE_FIELD = "postalCode"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<AddressDataResponse> {

            override fun deserialize(t: JSONObject): AddressDataResponse {
                return AddressDataResponse(
                    t.optNullableString(FIRST_NAME_FIELD),
                    t.optNullableString(LAST_NAME_FIELD),
                    t.optNullableString(ADDRESS_LINE_1_FIELD),
                    t.optNullableString(ADDRESS_LINE_2_FIELD),
                    t.optNullableString(POSTAL_CODE_FIELD),
                    t.optNullableString(CITY_FIELD),
                    t.optNullableString(STATE_FIELD),
                    t.optNullableString(COUNTRY_CODE_FIELD)?.let { CountryCode.valueOf(it) }
                )
            }
        }
    }
}
