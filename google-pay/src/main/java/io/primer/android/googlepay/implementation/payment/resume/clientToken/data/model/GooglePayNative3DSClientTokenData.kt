package io.primer.android.googlepay.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class GooglePayNative3DSClientTokenData(
    val intent: String,
    val supportedThreeDsProtocolVersions: List<String>
) : JSONDeserializable {

    companion object {

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): GooglePayNative3DSClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<GooglePayNative3DSClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val SUPPORTED_THREE_DS_PROTOCOL_VERSIONS_FIELD =
            "supportedThreeDsProtocolVersions"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            GooglePayNative3DSClientTokenData(
                intent = t.getString(INTENT_FIELD),
                supportedThreeDsProtocolVersions = t.getJSONArray(SUPPORTED_THREE_DS_PROTOCOL_VERSIONS_FIELD)
                    .sequence<String>()
                    .toList()
            )
        }
    }
}
