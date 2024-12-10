package io.primer.android.vault.implementation.vaultedMethods.data.model.empty

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.vault.implementation.vaultedMethods.data.model.BasePaymentMethodVaultExchangeDataRequest
import org.json.JSONObject

internal class EmptyExchangeDataRequest : BasePaymentMethodVaultExchangeDataRequest {

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<EmptyExchangeDataRequest> { JSONObject() }
    }
}
