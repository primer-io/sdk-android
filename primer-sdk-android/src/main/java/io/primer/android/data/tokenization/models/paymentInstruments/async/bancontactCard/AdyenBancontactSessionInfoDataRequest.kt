package io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class AdyenBancontactSessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String,
    val userAgent: String
) : BaseSessionInfoDataRequest(locale) {

    internal data class BrowserInfoDataRequest(private val userAgent: String) :
        JSONObjectSerializable {

        companion object {

            private const val USER_AGENT = "userAgent"

            @JvmField
            val serializer = object : JSONObjectSerializer<BrowserInfoDataRequest> {
                override fun serialize(t: BrowserInfoDataRequest): JSONObject {
                    return JSONObject().apply {
                        put(USER_AGENT, t.userAgent)
                    }
                }
            }
        }
    }

    companion object {

        private const val BROWSER_INFO = "browserInfo"

        @JvmField
        val serializer = object : JSONObjectSerializer<AdyenBancontactSessionInfoDataRequest> {
            override fun serialize(t: AdyenBancontactSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(LOCALE_FIELD, t.locale)
                    put(PLATFORM_FIELD, t.platform)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(
                        BROWSER_INFO,
                        JSONSerializationUtils.getJsonObjectSerializer<BrowserInfoDataRequest>()
                            .serialize(BrowserInfoDataRequest(t.userAgent))
                    )
                }
            }
        }
    }
}
