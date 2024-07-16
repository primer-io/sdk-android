package io.primer.android.data.tokenization.models.paymentInstruments.stripe.ach

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class StripeAchSessionInfoDataRequest(
    override val locale: String
) : BaseSessionInfoDataRequest(locale) {
    companion object {

        @JvmField
        val serializer =
            JSONObjectSerializer<StripeAchSessionInfoDataRequest> { t ->
                JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                }
            }
    }
}
