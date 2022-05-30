package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.Serializable

@Serializable
data class PrimerCountry(
    val name: String,
    val code: CountryCode
) {
    companion object {
        val default: PrimerCountry = PrimerCountry("United Kingdom", CountryCode.BG)
    }
}

data class PrimerCountriesCodeInfo(
    val locale: String,
    val countries: Map<String, JsonElement>
)
