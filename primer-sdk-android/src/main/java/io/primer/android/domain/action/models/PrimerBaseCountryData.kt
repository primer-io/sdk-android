package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.CountryCode

internal abstract class PrimerBaseCountryData {
    abstract val name: String
    abstract val code: CountryCode
}
