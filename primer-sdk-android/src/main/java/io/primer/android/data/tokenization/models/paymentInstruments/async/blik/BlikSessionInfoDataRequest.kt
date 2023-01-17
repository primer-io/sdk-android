package io.primer.android.data.tokenization.models.paymentInstruments.async.blik

import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class BlikSessionInfoDataRequest(
    val blikCode: String,
    override val locale: String,
    override val redirectionUrl: String,
    override val platform: String = "ANDROID"
) : BaseSessionInfoDataRequest(locale, redirectionUrl, platform) {
    companion object {

        private const val BLIK_CODE_FIELD = "blikCode"

        @JvmField
        val serializer = object : JSONSerializer<BlikSessionInfoDataRequest> {
            override fun serialize(t: BlikSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(BLIK_CODE_FIELD, t.blikCode)
                }
            }
        }
    }
}
