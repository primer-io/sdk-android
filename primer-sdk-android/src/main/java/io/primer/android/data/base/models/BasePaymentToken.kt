package io.primer.android.data.base.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.extensions.optNullableBoolean
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.threeds.data.models.common.ResponseCode
import org.json.JSONObject

internal abstract class BasePaymentToken : JSONDeserializable {
    abstract val token: String
    abstract val paymentMethodType: String?
    abstract val paymentInstrumentType: String
    abstract val paymentInstrumentData: PaymentInstrumentData?
    abstract val vaultData: VaultDataResponse?
    abstract val threeDSecureAuthentication: AuthenticationDetailsDataResponse?
    abstract val isVaulted: Boolean

    data class VaultDataResponse(
        val customerId: String
    ) : JSONDeserializable {
        companion object {
            private const val CUSTOMER_ID_FIELD = "customerId"

            @JvmField
            val deserializer = object : JSONObjectDeserializer<VaultDataResponse> {

                override fun deserialize(t: JSONObject): VaultDataResponse {
                    return VaultDataResponse(t.getString(CUSTOMER_ID_FIELD))
                }
            }
        }
    }

    data class AuthenticationDetailsDataResponse(
        val responseCode: ResponseCode,
        val reasonCode: String? = null,
        val reasonText: String? = null,
        val protocolVersion: String? = null,
        val challengeIssued: Boolean? = null
    ) : JSONDeserializable {
        companion object {
            private const val RESPONSE_CODE_FIELD = "responseCode"
            private const val REASON_CODE_FIELD = "reasonCode"
            private const val REASON_TEXT_FIELD = "reasonText"
            private const val PROTOCOL_VERSION_FIELD = "protocolVersion"
            private const val CHALLENGE_ISSUED_FIELD = "challengeIssued"

            @JvmField
            val deserializer = object : JSONObjectDeserializer<AuthenticationDetailsDataResponse> {

                override fun deserialize(t: JSONObject): AuthenticationDetailsDataResponse {
                    return AuthenticationDetailsDataResponse(
                        ResponseCode.valueOf(t.getString(RESPONSE_CODE_FIELD)),
                        t.optNullableString(REASON_CODE_FIELD),
                        t.optNullableString(REASON_TEXT_FIELD),
                        t.optNullableString(PROTOCOL_VERSION_FIELD),
                        t.optNullableBoolean(CHALLENGE_ISSUED_FIELD)
                    )
                }
            }
        }
    }
}
