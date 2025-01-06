package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class LocaleDataRequest(
    val countryCode: CountryCode?,
    val currencyCode: String,
    val localeCode: String,
) : JSONObjectSerializable {
    companion object {
        const val COUNTRY_CODE_FIELD = "countryCode"
        const val CURRENCY_CODE_FIELD = "currencyCode"
        const val LOCALE_CODE_FIELD = "localeCode"

        @JvmField
        val serializer =
            JSONObjectSerializer<LocaleDataRequest> { t ->
                JSONObject().apply {
                    putOpt(COUNTRY_CODE_FIELD, t.countryCode?.name)
                    put(CURRENCY_CODE_FIELD, t.currencyCode)
                    put(LOCALE_CODE_FIELD, t.localeCode)
                }
            }
    }
}
