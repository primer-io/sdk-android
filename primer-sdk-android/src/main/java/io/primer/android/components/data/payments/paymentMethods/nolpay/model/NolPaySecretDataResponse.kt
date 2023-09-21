package io.primer.android.components.data.payments.paymentMethods.nolpay.model

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer

internal data class NolPaySecretDataResponse(val sdkSecret: String) : JSONDeserializable {

    companion object {

        private const val SDK_SECRET_FIELD = "sdSecret"

        @JvmField
        val deserializer =
            JSONDeserializer { t -> NolPaySecretDataResponse(t.getString(SDK_SECRET_FIELD)) }
    }
}
