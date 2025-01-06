package io.primer.android.nolpay.implementation.common.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

internal data class NolPaySecretDataResponse(val sdkSecret: String) : JSONDeserializable {
    companion object {
        private const val SDK_SECRET_FIELD = "sdkSecret"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t -> NolPaySecretDataResponse(sdkSecret = t.getString(SDK_SECRET_FIELD)) }
    }
}
