package io.primer.android.vault.implementation.vaultedMethods.data.model.card

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.vault.implementation.vaultedMethods.data.model.BasePaymentMethodVaultExchangeDataRequest
import org.json.JSONObject

internal data class CardVaultExchangeDataRequest(val cvv: String) : BasePaymentMethodVaultExchangeDataRequest {

    companion object {

        private const val CVV_FIELD = "cvv"

        @JvmField
        val serializer = JSONObjectSerializer<CardVaultExchangeDataRequest> { t ->
            JSONObject().apply {
                put(CVV_FIELD, t.cvv)
            }
        }
    }
}
