package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.configuration.models.CountryCode
import org.json.JSONObject

internal data class LocaleDataRequest(
    val countryCode: CountryCode?,
    val currencyCode: String,
    val localeCode: String
) : JSONObjectSerializable {

    companion object {
        private const val COUNTRY_CODE_FIELD = "countryCode"
        private const val CURRENCY_CODE_FIELD = "currencyCode"
        private const val LOCALE_CODE_FIELD = "localeCode"

        @JvmField
        val serializer = object : JSONObjectSerializer<LocaleDataRequest> {
            override fun serialize(t: LocaleDataRequest): JSONObject {
                return JSONObject().apply {
                    putOpt(COUNTRY_CODE_FIELD, t.countryCode?.name)
                    put(CURRENCY_CODE_FIELD, t.currencyCode)
                    put(LOCALE_CODE_FIELD, t.localeCode)
                }
            }
        }
    }
}
