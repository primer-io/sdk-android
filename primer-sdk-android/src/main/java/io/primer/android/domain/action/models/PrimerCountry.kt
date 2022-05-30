package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PrimerCountry(
    val name: String,
    val code: CountryCode
) {
    companion object {
        val default: PrimerCountry = PrimerCountry("United Kingdom", CountryCode.BG)
    }
}

@Serializable
data class PrimerCountriesCodeInfo(
    val locale: String,
    val countries: Map<String, JsonElement>
)
