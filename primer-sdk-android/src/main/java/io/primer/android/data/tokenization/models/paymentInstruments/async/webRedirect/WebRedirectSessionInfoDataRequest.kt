package io.primer.android.data.tokenization.models.paymentInstruments.async.webRedirect

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class WebRedirectSessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String,
    override val platform: String
) : BaseSessionInfoDataRequest(locale) {

    companion object {
        @JvmField
        val serializer =
            JSONObjectSerializer<WebRedirectSessionInfoDataRequest> { t ->
                JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                }
            }
    }
}
