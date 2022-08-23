package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class PrimerCountry(
    override val name: String,
    override val code: CountryCode
) : PrimerBaseCountryData() {
    companion object {
        val default: PrimerCountry = PrimerCountry("United Kingdom", CountryCode.BG)
    }
}

@Serializable
data class PrimerCountriesCodeInfo(
    val locale: String,
    val countries: Map<String, JsonElement>
)
