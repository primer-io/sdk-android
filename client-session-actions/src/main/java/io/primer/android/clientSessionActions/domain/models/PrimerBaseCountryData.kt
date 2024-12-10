package io.primer.android.clientSessionActions.domain.models

import io.primer.android.configuration.data.model.CountryCode

internal interface PrimerBaseCountryData {
    val name: String
    val code: CountryCode
}
