package io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class AdyenBancontactSessionInfoDataRequest(
    val userAgent: String,
    override val locale: String,
    override val redirectionUrl: String,
    override val platform: String = "ANDROID"
) : BaseSessionInfoDataRequest(locale, redirectionUrl, platform) {

    internal data class BrowserInfoDataRequest(private val userAgent: String) : JSONSerializable {

        companion object {

            private const val USER_AGENT = "userAgent"

            @JvmField
            val serializer = object : JSONSerializer<BrowserInfoDataRequest> {
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
        val serializer = object : JSONSerializer<AdyenBancontactSessionInfoDataRequest> {
            override fun serialize(t: AdyenBancontactSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(LOCALE_FIELD, t.locale)
                    put(PLATFORM_FIELD, t.platform)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(
                        BROWSER_INFO,
                        JSONSerializationUtils.getSerializer<BrowserInfoDataRequest>()
                            .serialize(BrowserInfoDataRequest(t.userAgent))
                    )
                }
            }
        }
    }
}
