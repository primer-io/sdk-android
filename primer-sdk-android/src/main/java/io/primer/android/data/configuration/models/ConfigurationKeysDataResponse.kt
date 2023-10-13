package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class ConfigurationKeysDataResponse(
    internal val threeDSecureIoCertificates: List<ThreeDsSecureCertificateDataResponse>?,
    internal val netceteraLicenseKey: String?
) : JSONDeserializable {
    companion object {
        private const val THREE_DS_CERTIFICATES_FIELD = "threeDSecureIoCertificates"
        private const val NETCETERA_LICENCE_KEY = "netceteraLicenseKey"

        @JvmField
        val deserializer = object :
            JSONDeserializer<ConfigurationKeysDataResponse> {

            override fun deserialize(t: JSONObject): ConfigurationKeysDataResponse {
                return ConfigurationKeysDataResponse(
                    t.optJSONArray(THREE_DS_CERTIFICATES_FIELD)?.sequence<JSONObject>()?.map {
                        JSONSerializationUtils
                            .getDeserializer<ThreeDsSecureCertificateDataResponse>()
                            .deserialize(it)
                    }?.toList(),
                    t.optNullableString(NETCETERA_LICENCE_KEY)
                )
            }
        }
    }
}

internal data class ThreeDsSecureCertificateDataResponse(
    val cardNetwork: String,
    val rootCertificate: String,
    val encryptionKey: String
) : JSONDeserializable {

    companion object {
        private const val CARD_NETWORK_FIELD = "cardNetwork"
        private const val ROOT_CERTIFICATE_FIELD = "rootCertificate"
        private const val ENCRYPTION_KEY_FIELD = "encryptionKey"

        @JvmField
        val deserializer = object : JSONDeserializer<ThreeDsSecureCertificateDataResponse> {

            override fun deserialize(t: JSONObject): ThreeDsSecureCertificateDataResponse {
                return ThreeDsSecureCertificateDataResponse(
                    t.getString(CARD_NETWORK_FIELD),
                    t.getString(ROOT_CERTIFICATE_FIELD),
                    t.getString(ENCRYPTION_KEY_FIELD)
                )
            }
        }
    }
}
