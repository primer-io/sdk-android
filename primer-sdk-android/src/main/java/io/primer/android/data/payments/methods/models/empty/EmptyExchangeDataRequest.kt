package io.primer.android.data.payments.methods.models.empty

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.payments.methods.models.BasePaymentMethodVaultExchangeDataRequest
import org.json.JSONObject

internal class EmptyExchangeDataRequest : BasePaymentMethodVaultExchangeDataRequest {

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<EmptyExchangeDataRequest> { JSONObject() }
    }
}
