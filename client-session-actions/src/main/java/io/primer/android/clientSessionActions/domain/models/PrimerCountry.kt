package io.primer.android.clientSessionActions.domain.models

import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.toMap

data class PrimerCountry(
    override val name: String,
    override val code: CountryCode,
) : PrimerBaseCountryData, JSONDeserializable {
    companion object {
        val default: PrimerCountry = PrimerCountry("United Kingdom", CountryCode.BG)

        private const val NAME_FIELD = "name"
        private const val COUNTRY_CODE_FIELD = "code"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PrimerCountry(
                    t.getString(NAME_FIELD),
                    CountryCode.valueOf(t.getString(COUNTRY_CODE_FIELD)),
                )
            }
    }
}

data class PrimerCountriesCodeInfo(
    val locale: String,
    val countries: Map<String, *>,
) : JSONDeserializable {
    companion object {
        private const val LOCALE_FIELD = "locale"
        private const val COUNTRIES_FIELD = "countries"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PrimerCountriesCodeInfo(
                    t.getString(LOCALE_FIELD),
                    t.getJSONObject(COUNTRIES_FIELD).toMap(),
                )
            }
    }
}
