package io.primer.android.banks.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class BankIssuerSessionInfoDataRequest(
    val redirectionUrl: String,
    val locale: String,
    val issuer: String,
    val platform: String = "ANDROID",
) : JSONObjectSerializable {
    companion object {
        private const val PLATFORM_FIELD = "platform"
        private const val LOCALE_FIELD = "locale"
        private const val REDIRECTION_URL_FIELD = "redirectionUrl"
        private const val ISSUER_FIELD = "issuer"

        @JvmField
        val serializer =
            JSONObjectSerializer<BankIssuerSessionInfoDataRequest> { t ->
                JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(ISSUER_FIELD, t.issuer)
                }
            }
    }
}
