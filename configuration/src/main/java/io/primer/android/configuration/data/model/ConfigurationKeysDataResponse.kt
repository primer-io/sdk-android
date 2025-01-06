package io.primer.android.configuration.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence
import org.json.JSONObject

data class ConfigurationKeysDataResponse(
    val threeDSecureIoCertificates: List<ThreeDsSecureCertificateDataResponse>?,
    val netceteraApiKey: String?,
) : JSONDeserializable {
    companion object {
        private const val THREE_DS_CERTIFICATES_FIELD = "threeDSecureIoCertificates"
        private const val NETCETERA_API_KEY = "netceteraApiKey"

        @JvmField
        val deserializer =
            JSONObjectDeserializer<ConfigurationKeysDataResponse> { t ->
                ConfigurationKeysDataResponse(
                    t.optJSONArray(THREE_DS_CERTIFICATES_FIELD)?.sequence<JSONObject>()?.map {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<ThreeDsSecureCertificateDataResponse>()
                            .deserialize(it)
                    }?.toList(),
                    t.optNullableString(NETCETERA_API_KEY),
                )
            }
    }
}

data class ThreeDsSecureCertificateDataResponse(
    val cardNetwork: String,
    val rootCertificate: String,
    val encryptionKey: String,
) : JSONDeserializable {
    companion object {
        private const val CARD_NETWORK_FIELD = "cardNetwork"
        private const val ROOT_CERTIFICATE_FIELD = "rootCertificate"
        private const val ENCRYPTION_KEY_FIELD = "encryptionKey"

        @JvmField
        val deserializer =
            object : JSONObjectDeserializer<ThreeDsSecureCertificateDataResponse> {
                override fun deserialize(t: JSONObject): ThreeDsSecureCertificateDataResponse {
                    return ThreeDsSecureCertificateDataResponse(
                        t.getString(CARD_NETWORK_FIELD),
                        t.getString(ROOT_CERTIFICATE_FIELD),
                        t.getString(ENCRYPTION_KEY_FIELD),
                    )
                }
            }
    }
}
