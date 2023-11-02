package io.primer.android.data.tokenization.models.paymentInstruments.async.retailOutlets

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class RetailOutletsSessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String,
    val retailOutlet: String
) : BaseSessionInfoDataRequest(locale, redirectionUrl) {
    companion object {

        private const val RETAIL_OUTLET_FIELD = "retailOutlet"

        @JvmField
        val serializer = object : JSONObjectSerializer<RetailOutletsSessionInfoDataRequest> {
            override fun serialize(t: RetailOutletsSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(RETAIL_OUTLET_FIELD, t.retailOutlet)
                }
            }
        }
    }
}
