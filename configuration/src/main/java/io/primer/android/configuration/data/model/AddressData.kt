package io.primer.android.configuration.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.domain.action.models.PrimerAddress
import org.json.JSONObject

data class AddressData(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val countryCode: CountryCode? = null,
) : JSONObjectSerializable, JSONDeserializable {
    fun toAddress() =
        PrimerAddress(
            firstName,
            lastName,
            addressLine1,
            addressLine2,
            postalCode,
            city,
            state,
            countryCode,
        )

    companion object {
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"
        private const val ADDRESS_LINE_1_FIELD = "addressLine1"
        private const val ADDRESS_LINE_2_FIELD = "addressLine2"
        private const val ADDRESS_LINE_3_FIELD = "addressLine3"
        private const val CITY_FIELD = "city"
        private const val STATE_FIELD = "state"
        private const val COUNTRY_CODE_FIELD = "countryCode"
        private const val POSTAL_CODE_FIELD = "postalCode"
        private const val EMAIL_FIELD = "email"
        private const val PHONE_NUMBER_FIELD = "phoneNumber"

        @JvmField
        val serializer =
            JSONObjectSerializer<AddressData> { t ->
                JSONObject().apply {
                    putOpt(FIRST_NAME_FIELD, t.firstName)
                    putOpt(LAST_NAME_FIELD, t.lastName)
                    putOpt(EMAIL_FIELD, t.email)
                    putOpt(PHONE_NUMBER_FIELD, t.phoneNumber)
                    put(ADDRESS_LINE_1_FIELD, t.addressLine1)
                    putOpt(ADDRESS_LINE_2_FIELD, t.addressLine2)
                    putOpt(ADDRESS_LINE_3_FIELD, t.addressLine3)
                    put(CITY_FIELD, t.city)
                    putOpt(STATE_FIELD, t.state)
                    put(COUNTRY_CODE_FIELD, t.countryCode)
                    put(POSTAL_CODE_FIELD, t.postalCode)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                AddressData(
                    firstName = t.optNullableString(FIRST_NAME_FIELD),
                    lastName = t.optNullableString(LAST_NAME_FIELD),
                    email = t.optNullableString(EMAIL_FIELD),
                    phoneNumber = t.optNullableString(PHONE_NUMBER_FIELD),
                    addressLine1 = t.optNullableString(ADDRESS_LINE_1_FIELD),
                    addressLine2 = t.optNullableString(ADDRESS_LINE_2_FIELD),
                    addressLine3 = t.optNullableString(ADDRESS_LINE_3_FIELD),
                    postalCode = t.optNullableString(POSTAL_CODE_FIELD),
                    city = t.optNullableString(CITY_FIELD),
                    state = t.optNullableString(STATE_FIELD),
                    countryCode =
                    t.optNullableString(COUNTRY_CODE_FIELD)
                        ?.let { CountryCode.valueOf(it) },
                )
            }
    }
}
