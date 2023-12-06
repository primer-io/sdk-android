package io.primer.android.data.tokenization.models.paymentInstruments.async.blik

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class BlikSessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String,
    val blikCode: String
) : BaseSessionInfoDataRequest(locale) {
    companion object {

        private const val BLIK_CODE_FIELD = "blikCode"

        @JvmField
        val serializer = object : JSONObjectSerializer<BlikSessionInfoDataRequest> {
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
