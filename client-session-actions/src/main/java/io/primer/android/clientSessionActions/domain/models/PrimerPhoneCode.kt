package io.primer.android.clientSessionActions.domain.models

import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

data class PrimerPhoneCode(
    override val name: String,
    override val code: CountryCode,
    val dialCode: String
) : PrimerBaseCountryData, JSONDeserializable {

    companion object {
        val default: PrimerPhoneCode = PrimerPhoneCode("United Kingdom", CountryCode.BG, "+44")

        private const val NAME_FIELD = "name"
        private const val CODE_FIELD = "code"
        private const val DIAL_CODE_FIELD = "dial_code"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            PrimerPhoneCode(
                t.getString(NAME_FIELD),
                CountryCode.valueOf(t.getString(CODE_FIELD)),
                t.getString(DIAL_CODE_FIELD)
            )
        }
    }
}
