package io.primer.android.threeds.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class AuthenticationDataResponse(
    val acsReferenceNumber: String? = null,
    val acsSignedContent: String? = null,
    val acsTransactionId: String? = null,
    val responseCode: ResponseCode,
    val transactionId: String?,
    val acsOperatorId: String?,
    val dsReferenceNumber: String? = null,
    val dsTransactionId: String? = null,
    val eci: String? = null,
    val protocolVersion: String? = null,
    // skipped
    val skippedReasonCode: SkippedCode? = null,
    val skippedReasonText: String? = null,
    // declined
    val declinedReasonCode: DeclinedReasonCode? = null,
    val declinedReasonText: String? = null,
) : JSONDeserializable {

    companion object {
        private const val ACS_REFERENCE_NUMBER_FIELD = "acsReferenceNumber"
        private const val ACS_SIGNED_CONTENT_FIELD = "acsSignedContent"
        private const val ACS_TRANSACTION_ID_FIELD = "acsTransactionId"
        private const val RESPONSE_CODE_FIELD = "responseCode"
        private const val TRANSACTION_ID_FIELD = "transactionId"
        private const val ACS_OPERATOR_ID_FIELD = "acsOperatorId"
        private const val DS_REFERENCE_NUMBER_FIELD = "dsReferenceNumber"
        private const val DS_TRANSACTION_ID_FIELD = "dsTransactionId"
        private const val ECI_FIELD = "eci"
        private const val PROTOCOL_VERSION_FIELD = "protocolVersion"
        private const val SKIPPED_REASON_CODE_FIELD = "skippedReasonCode"
        private const val SKIPPED_REASON_TEXT_FIELD = "skippedReasonText"
        private const val DECLINED_REASON_CODE_FIELD = "declinedReasonCode"
        private const val DECLINED_REASON_TEXT_FIELD = "declinedReasonText"

        @JvmField
        val deserializer = object : JSONDeserializer<AuthenticationDataResponse> {

            override fun deserialize(t: JSONObject): AuthenticationDataResponse {
                return AuthenticationDataResponse(
                    t.optNullableString(ACS_REFERENCE_NUMBER_FIELD),
                    t.optNullableString(ACS_SIGNED_CONTENT_FIELD),
                    t.optNullableString(ACS_TRANSACTION_ID_FIELD),
                    ResponseCode.valueOf(t.getString(RESPONSE_CODE_FIELD)),
                    t.optNullableString(TRANSACTION_ID_FIELD),
                    t.optNullableString(ACS_OPERATOR_ID_FIELD),
                    t.optNullableString(DS_REFERENCE_NUMBER_FIELD),
                    t.optNullableString(DS_TRANSACTION_ID_FIELD),
                    t.optNullableString(ECI_FIELD),
                    t.optNullableString(PROTOCOL_VERSION_FIELD),
                    t.optNullableString(SKIPPED_REASON_CODE_FIELD)?.let { SkippedCode.valueOf(it) },
                    t.optNullableString(SKIPPED_REASON_TEXT_FIELD),
                    t.optNullableString(DECLINED_REASON_CODE_FIELD)
                        ?.let { DeclinedReasonCode.valueOf(it) },
                    t.optNullableString(DECLINED_REASON_TEXT_FIELD),
                )
            }
        }
    }
}
