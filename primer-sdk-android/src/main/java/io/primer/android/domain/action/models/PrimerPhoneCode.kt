package io.primer.android.domain.action.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.data.configuration.models.CountryCode
import org.json.JSONObject

internal data class PrimerPhoneCode(
    override val name: String,
    override val code: CountryCode,
    val dialCode: String
) : PrimerBaseCountryData, JSONDeserializable {

    internal companion object {
        val default: PrimerPhoneCode = PrimerPhoneCode("United Kingdom", CountryCode.BG, "+44")

        private const val NAME_FIELD = "name"
        private const val CODE_FIELD = "code"
        private const val DIAL_CODE_FIELD = "dial_code"

        @JvmField
        val deserializer = object : JSONDeserializer<PrimerPhoneCode> {
            override fun deserialize(t: JSONObject): PrimerPhoneCode {
                return PrimerPhoneCode(
                    t.getString(NAME_FIELD),
                    CountryCode.valueOf(t.getString(CODE_FIELD)),
                    t.getString(DIAL_CODE_FIELD),
                )
            }
        }
    }
}
