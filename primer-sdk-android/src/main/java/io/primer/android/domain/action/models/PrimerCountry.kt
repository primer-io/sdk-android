package io.primer.android.domain.action.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.extensions.toMap
import io.primer.android.data.configuration.models.CountryCode
import org.json.JSONObject

internal data class PrimerCountry(
    override val name: String,
    override val code: CountryCode
) : PrimerBaseCountryData, JSONDeserializable {
    companion object {
        val default: PrimerCountry = PrimerCountry("United Kingdom", CountryCode.BG)

        private const val NAME_FIELD = "name"
        private const val COUNTRY_CODE_FIELD = "code"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<PrimerCountry> {
            override fun deserialize(t: JSONObject): PrimerCountry {
                return PrimerCountry(
                    t.getString(NAME_FIELD),
                    CountryCode.valueOf(t.getString(COUNTRY_CODE_FIELD))
                )
            }
        }
    }
}

internal data class PrimerCountriesCodeInfo(
    val locale: String,
    val countries: Map<String, *>
) : JSONDeserializable {

    companion object {
        private const val LOCALE_FIELD = "locale"
        private const val COUNTRIES_FIELD = "countries"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<PrimerCountriesCodeInfo> {
            override fun deserialize(t: JSONObject): PrimerCountriesCodeInfo {
                return PrimerCountriesCodeInfo(
                    t.getString(LOCALE_FIELD),
                    t.getJSONObject(COUNTRIES_FIELD).toMap()
                )
            }
        }
    }
}
