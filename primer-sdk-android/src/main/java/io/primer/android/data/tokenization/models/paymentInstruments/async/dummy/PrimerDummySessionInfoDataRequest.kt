package io.primer.android.data.tokenization.models.paymentInstruments.async.dummy

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import io.primer.android.payment.dummy.DummyDecisionType
import org.json.JSONObject

internal data class PrimerDummySessionInfoDataRequest(
    override val locale: String,
    val redirectionUrl: String,
    val flowDecision: DummyDecisionType,
) : BaseSessionInfoDataRequest(locale, redirectionUrl) {
    companion object {

        private const val DECISION_FIELD = "flowDecision"

        @JvmField
        val serializer = object : JSONObjectSerializer<PrimerDummySessionInfoDataRequest> {
            override fun serialize(t: PrimerDummySessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(DECISION_FIELD, t.flowDecision.name)
                }
            }
        }
    }
}
