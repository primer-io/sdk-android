package io.primer.android.webredirect.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class WebRedirectSessionInfoDataRequest(
    val redirectionUrl: String,
    val locale: String,
    val platform: String = "ANDROID",
) : JSONObjectSerializable {
    companion object {
        private const val PLATFORM_FIELD = "platform"
        private const val LOCALE_FIELD = "locale"
        private const val REDIRECTION_URL_FIELD = "redirectionUrl"

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
