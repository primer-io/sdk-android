package io.primer.android.data.tokenization.models.paymentInstruments.async.bankIssuer

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class BankIssuerSessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String,
    val issuer: String
) : BaseSessionInfoDataRequest(locale) {
    companion object {

        private const val ISSUER_FIELD = "issuer"

        @JvmField
        val serializer = object : JSONObjectSerializer<BankIssuerSessionInfoDataRequest> {
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
