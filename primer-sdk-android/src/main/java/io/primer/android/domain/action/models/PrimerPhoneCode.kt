package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrimerPhoneCode(
    override val name: String,
    override val code: CountryCode,
    @SerialName("dial_code") val dialCode: String
) : PrimerBaseCountryData() {

    companion object {
        val default: PrimerPhoneCode = PrimerPhoneCode("United Kingdom", CountryCode.BG, "+44")
    }
}
