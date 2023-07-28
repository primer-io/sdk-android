package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode

internal interface PrimerBaseCountryData {
    val name: String
    val code: CountryCode
}
