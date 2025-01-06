package io.primer.android.bancontact.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class AdyenBancontactSessionInfoDataRequest(
    val locale: String,
    val redirectionUrl: String,
    val userAgent: String,
    val platform: String = "ANDROID",
) : JSONObjectSerializable {
    internal data class BrowserInfoDataRequest(private val userAgent: String) :
        JSONObjectSerializable {
        companion object {
            private const val USER_AGENT = "userAgent"

            @JvmField
            val serializer =
                JSONObjectSerializer<BrowserInfoDataRequest> { t ->
                    JSONObject().apply {
                        put(USER_AGENT, t.userAgent)
                    }
                }
        }
    }

    companion object {
        private const val PLATFORM_FIELD = "platform"
        private const val LOCALE_FIELD = "locale"
        private const val REDIRECTION_URL_FIELD = "redirectionUrl"
        private const val BROWSER_INFO = "browserInfo"

        @JvmField
        val serializer =
            JSONObjectSerializer<AdyenBancontactSessionInfoDataRequest> { t ->
                JSONObject().apply {
                    put(LOCALE_FIELD, t.locale)
                    put(PLATFORM_FIELD, t.platform)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(
                        BROWSER_INFO,
                        JSONSerializationUtils.getJsonObjectSerializer<BrowserInfoDataRequest>()
                            .serialize(BrowserInfoDataRequest(t.userAgent)),
                    )
                }
            }
    }
}
