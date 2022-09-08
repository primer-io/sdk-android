package io.primer.android.threeds.data.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class Address(
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
) : JSONSerializable {

    companion object {
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"
        private const val EMAIL_FIELD = "email"
        private const val PHONE_NUMBER_FIELD = "phoneNumber"
        private const val ADDRESS_LINE_1_FIELD = "addressLine1"
        private const val ADDRESS_LINE_2_FIELD = "addressLine2"
        private const val ADDRESS_LINE_3_FIELD = "addressLine3"
        private const val CITY_FIELD = "city"
        private const val STATE_FIELD = "state"
        private const val COUNTRY_CODE_FIELD = "countryCode"
        private const val POSTAL_CODE_FIELD = "postalCode"

        @JvmField
        val serializer = object : JSONSerializer<Address> {
            override fun serialize(t: Address): JSONObject {
                return JSONObject().apply {
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
        }
    }
}
