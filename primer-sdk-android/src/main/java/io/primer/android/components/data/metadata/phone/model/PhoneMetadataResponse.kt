package io.primer.android.components.data.metadata.phone.model

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.extensions.optNullableString

internal data class PhoneMetadataResponse(
    val isValid: Boolean,
    val countryCode: String?,
    val nationalNumber: String?
) : JSONDeserializable {

    companion object {

        private const val VALID_FIELD = "isValid"
        private const val COUNTRY_CODE_FIELD = "countryCode"
        private const val NATIONAL_NUMBER_FIELD = "nationalNumber"

        @JvmField
        val deserializer = JSONObjectDeserializer {
            PhoneMetadataResponse(
                it.getBoolean(VALID_FIELD),
                it.optNullableString(COUNTRY_CODE_FIELD),
                it.optNullableString(NATIONAL_NUMBER_FIELD)
            )
        }
    }
}
