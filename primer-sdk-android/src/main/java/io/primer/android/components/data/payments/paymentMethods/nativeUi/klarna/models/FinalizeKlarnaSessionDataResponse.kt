package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils

internal data class FinalizeKlarnaSessionDataResponse(
    val sessionData: KlarnaSessionData
) : JSONDeserializable {
    companion object {
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val deserializer =
            JSONObjectDeserializer<FinalizeKlarnaSessionDataResponse> { t ->
                FinalizeKlarnaSessionDataResponse(
                    JSONSerializationUtils.getJsonObjectDeserializer<KlarnaSessionData>()
                        .deserialize(
                            t.getJSONObject(SESSION_DATA_FIELD)
                        )
                )
            }
    }
}
