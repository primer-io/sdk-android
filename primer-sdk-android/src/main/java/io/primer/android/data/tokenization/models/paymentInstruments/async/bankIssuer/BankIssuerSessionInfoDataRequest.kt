package io.primer.android.data.tokenization.models.paymentInstruments.async.bankIssuer

import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class BankIssuerSessionInfoDataRequest(
    val issuer: String,
    override val locale: String,
    override val redirectionUrl: String,
    override val platform: String = "ANDROID"
) : BaseSessionInfoDataRequest(locale, redirectionUrl, platform) {
    companion object {

        private const val ISSUER_FIELD = "issuer"

        @JvmField
        val serializer = object : JSONSerializer<BankIssuerSessionInfoDataRequest> {
            override fun serialize(t: BankIssuerSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(ISSUER_FIELD, t.issuer)
                }
            }
        }
    }
}
