package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils

internal data class FinalizeKlarnaSessionDataResponse(
    val sessionData: KlarnaSessionData,
) : JSONDeserializable {
    companion object {
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                FinalizeKlarnaSessionDataResponse(
                    JSONSerializationUtils.getJsonObjectDeserializer<KlarnaSessionData>()
                        .deserialize(
                            t.getJSONObject(SESSION_DATA_FIELD),
                        ),
                )
            }
    }
}
