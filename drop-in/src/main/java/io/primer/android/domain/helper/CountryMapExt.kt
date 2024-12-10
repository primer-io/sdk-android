package io.primer.android.domain.helper

import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.configuration.data.model.emojiFlag
import io.primer.android.ui.fragments.country.CountryCodeItem
import java.util.Locale

internal fun List<PrimerCountry>.mapCountryToCountryItem(): List<CountryCodeItem> {
    return map {
        CountryCodeItem(
            it.code,
            String.format(Locale.getDefault(), "%s %s", it.code.emojiFlag(), it.name)
        )
    }
}

internal fun List<PrimerPhoneCode>.mapPhoneCodesToCountryItem(): List<CountryCodeItem> {
    return map {
        CountryCodeItem(
            it.code,
            String.format(
                Locale.getDefault(),
                "%s %s (%s)",
                it.code.emojiFlag(),
                it.name,
                it.dialCode
            )
        )
    }
}
