package io.primer.android.components.data.payments.paymentMethods.nolpay.model

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer

internal data class NolPaySecretDataResponse(val appSecret: String) : JSONDeserializable {

    companion object {

        private const val APP_SECRET_FIELD = "appSecret"

        @JvmField
        val serializer =
            JSONDeserializer { t -> NolPaySecretDataResponse(t.getString(APP_SECRET_FIELD)) }
    }
}
