package io.primer.android.data.tokenization.models.paymentInstruments.async.webRedirect

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class WebRedirectSessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String
) : BaseSessionInfoDataRequest(locale, redirectionUrl) {

    companion object {
        @JvmField
        val serializer = object : JSONObjectSerializer<WebRedirectSessionInfoDataRequest> {
            override fun serialize(t: WebRedirectSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                }
            }
        }
    }
}
