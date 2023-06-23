package io.primer.android.data.payments.methods.models.card

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.payments.methods.models.BasePaymentMethodVaultExchangeDataRequest
import org.json.JSONObject

internal data class CardVaultExchangeDataRequest(val cvv: String) :
    BasePaymentMethodVaultExchangeDataRequest {

    companion object {

        private const val CVV_FIELD = "cvv"

        @JvmField
        val serializer = object : JSONObjectSerializer<CardVaultExchangeDataRequest> {
            override fun serialize(t: CardVaultExchangeDataRequest): JSONObject {
                return JSONObject().apply {
                    put(CVV_FIELD, t.cvv)
                }
            }
        }
    }
}
